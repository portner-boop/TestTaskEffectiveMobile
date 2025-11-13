package com.example.testtaskeffectivemobile.service;

import com.example.testtaskeffectivemobile.dto.request.AuthenticationRequest;
import com.example.testtaskeffectivemobile.dto.request.RefreshRequest;
import com.example.testtaskeffectivemobile.dto.request.RegistrationRequest;
import com.example.testtaskeffectivemobile.dto.response.AuthenticationResponse;
import com.example.testtaskeffectivemobile.entity.Role;
import com.example.testtaskeffectivemobile.entity.User;
import com.example.testtaskeffectivemobile.exception.BusinessException;
import com.example.testtaskeffectivemobile.exception.ErrorCode;
import com.example.testtaskeffectivemobile.mapper.UserMapper;
import com.example.testtaskeffectivemobile.repository.RoleRepository;
import com.example.testtaskeffectivemobile.repository.UserRepository;
import com.example.testtaskeffectivemobile.security.JwtService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    @Override
    public AuthenticationResponse login(AuthenticationRequest request) {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        final User user = (User) authentication.getPrincipal();
        final String accessToken =this.jwtService.generateAccessToken(user.getUsername());
        final String refreshToken =this.jwtService.generateRefreshToken(user.getUsername());
        final String tokenType = "Bearer";

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .build();
    }

    @Override
    @Transactional
    public void register(RegistrationRequest request) {
        checkUserEmail(request.getEmail());
        checkPasswords(request.getPassword(), request.getConfirmPassword());
        final Role userRole;
        if (request.getEmail() != null && request.getEmail().contains("admin")) {
            userRole = this.roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new EntityNotFoundException("Role does not exist"));
        }
        else {
            userRole = this.roleRepository.findByName("ROLE_GUEST")
                    .orElseThrow(() -> new EntityNotFoundException("Role does not exist"));
        }
        final List<Role> roles = new ArrayList<>();
        roles.add(userRole);

        final User user = this.userMapper.toUser(request);
        user.setRoles(roles);
        log.debug("Register user: {}", user);
        userRepository.save(user);
        log.debug("Registered user: {}", user);
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request) {
        final String newAccessToken = this.jwtService.refreshToken(request.getRefreshToken());
        final String tokenType = "Bearer";
        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType(tokenType)
                .build();
    }

    @Override
    public void logout(String userId){
        jwtService.dropAllTokens(userId);
    }


    private void checkPasswords(String password, String confirmPassword) {
        if (password == null || !password.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
    }

    private void checkUserEmail(String email) {
        final boolean emailExists = this.userRepository.findByEmailIgnoreCase(email).isPresent();
        if (emailExists) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }
}