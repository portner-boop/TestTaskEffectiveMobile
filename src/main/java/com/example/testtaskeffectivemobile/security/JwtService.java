package com.example.testtaskeffectivemobile.security;

import com.example.testtaskeffectivemobile.entity.AccessToken;
import com.example.testtaskeffectivemobile.entity.RefreshToken;
import com.example.testtaskeffectivemobile.entity.Role;
import com.example.testtaskeffectivemobile.entity.User;
import com.example.testtaskeffectivemobile.exception.BusinessException;
import com.example.testtaskeffectivemobile.exception.ErrorCode;
import com.example.testtaskeffectivemobile.repository.AccessTokenRepository;
import com.example.testtaskeffectivemobile.repository.RefreshTokenRepository;
import com.example.testtaskeffectivemobile.repository.UserRepository;
import com.example.testtaskeffectivemobile.util.KeyUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.testtaskeffectivemobile.entity.AccessToken;
import com.example.testtaskeffectivemobile.entity.RefreshToken;
import com.example.testtaskeffectivemobile.entity.Role;
import com.example.testtaskeffectivemobile.entity.User;
import com.example.testtaskeffectivemobile.exception.BusinessException;
import com.example.testtaskeffectivemobile.exception.ErrorCode;
import com.example.testtaskeffectivemobile.repository.AccessTokenRepository;
import com.example.testtaskeffectivemobile.repository.RefreshTokenRepository;
import com.example.testtaskeffectivemobile.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final String TOKEN_TYPE = "token_type";
    private static final String ROLES = "role_list";

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    private final long accessTokenExpiration = 3600000L;
    private final long refreshTokenExpiration = 604800000L;

    public JwtService(
            AccessTokenRepository accessTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository
    ) {
        this.accessTokenRepository = accessTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;

        try {
            KeyPair keyPair = generateRsaKeyPair();
            this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
            this.publicKey = (RSAPublicKey) keyPair.getPublic();
            log.info("JWT keys generated successfully (hardcoded mode)");
        } catch (Exception e) {
            log.error("Failed to generate JWT keys", e);
            throw new RuntimeException("JWT initialization failed", e);
        }
    }

    // Метод для генерации RSA ключей (2048-bit)
    private KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    // Остальные методы без изменений
    public String generateAccessToken(final String userName) {
        final User savedUser = userRepository.findByEmailIgnoreCase(userName)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userName));
        final Map<String, Object> claims = Map.of(
                TOKEN_TYPE, "ACCESS_TOKEN",
                ROLES, String.join(" | ", savedUser.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList())));
        final String accessToken = buildToken(userName, claims, this.accessTokenExpiration);
        AccessToken token = accessTokenRepository.findById(userName)
                .orElse(AccessToken.builder()
                        .id(userName)
                        .accessToken(new ArrayList<>())
                        .build());
        token.getAccessToken().add(accessToken);
        accessTokenRepository.save(token);
        return accessToken;
    }

    public String generateRefreshToken(final String userName) {
        final Map<String, Object> claims = Map.of(TOKEN_TYPE, "REFRESH_TOKEN");
        final String refreshToken = buildToken(userName, claims, this.refreshTokenExpiration);
        RefreshToken token = refreshTokenRepository.findById(userName)
                .orElse(RefreshToken.builder()
                        .id(userName)
                        .build());
        token.setRefreshToken(refreshToken);
        refreshTokenRepository.save(token);
        return refreshToken;
    }

    public void dropAllTokens(final String userName) {
        refreshTokenRepository.deleteById(userName);
        accessTokenRepository.deleteById(userName);
    }

    private String buildToken(String userName, Map<String, Object> claims, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(userName)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(this.privateKey)
                .compact();
    }

    public boolean validateToken(final String token, final String expectedUserName) {
        final String userName = extractUserName(token);
        return userName.equals(expectedUserName) && !isTokenExpired(token) && !isAccessTokenWithdrown(token, userName);
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private boolean isRefreshTokenWithdrown(String refreshToken, String userName) {
        RefreshToken token = this.refreshTokenRepository.findById(userName).orElse(null);
        return token == null || !token.getRefreshToken().equals(refreshToken);
    }

    private boolean isAccessTokenWithdrown(String accessToken, String userName) {
        AccessToken token = this.accessTokenRepository.findById(userName).orElse(null);
        return token == null || !token.getAccessToken().contains(accessToken);
    }

    public String extractUserName(String token) {
        return extractClaims(token).getSubject();
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (final JwtException ex) {
            throw new RuntimeException("Invalid JWT token", ex);
        }
    }

    public String refreshToken(final String refreshToken) {
        final Claims claims = extractClaims(refreshToken);
        final String userName = claims.getSubject();
        if (!"REFRESH_TOKEN".equals(claims.get(TOKEN_TYPE))) {
            throw new RuntimeException("Invalid refresh token");
        }
        if (isTokenExpired(refreshToken) || isRefreshTokenWithdrown(refreshToken, userName)) {
            throw new RuntimeException("Refresh token expired");
        }
        return generateAccessToken(userName);
    }
}