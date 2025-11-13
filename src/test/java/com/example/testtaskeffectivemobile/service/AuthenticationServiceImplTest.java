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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private AuthenticationRequest authRequest;
    private RegistrationRequest registrationRequest;
    private User user;
    private Role userRole;

    @BeforeEach
    void setUp() {
        authRequest = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        registrationRequest = RegistrationRequest.builder()
                .email("test@example.com")
                .password("password")
                .confirmPassword("password")
                .firstName("John")
                .lastName("Doe")
                .build();

        userRole = Role.builder()
                .id(1L)
                .name("ROLE_GUEST")
                .build();

        user = User.builder()
                .id("user123")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .enabled(true)
                .locked(false)
                .credentialsExpired(false)
                .roles(List.of(userRole))
                .build();
    }

    @Test
    void login_Success() {

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtService.generateAccessToken(user.getUsername())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user.getUsername())).thenReturn("refreshToken");
        AuthenticationResponse response = authenticationService.login(authRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateAccessToken(user.getUsername());
        verify(jwtService).generateRefreshToken(user.getUsername());
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authenticationService.login(authRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void register_Success() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_GUEST")).thenReturn(Optional.of(userRole));
        when(userMapper.toUser(any(RegistrationRequest.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        authenticationService.register(registrationRequest);

        verify(userRepository).findByEmailIgnoreCase(registrationRequest.getEmail());
        verify(roleRepository).findByName("ROLE_GUEST");
        verify(userMapper).toUser(registrationRequest);
        verify(userRepository).save(user);
    }

    @Test
    void register_EmailAlreadyExists_ThrowsBusinessException() {

        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(user));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authenticationService.register(registrationRequest));

        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, exception.getErrorCode());
        verify(userRepository).findByEmailIgnoreCase(registrationRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_PasswordMismatch_ThrowsBusinessException() {
        registrationRequest.setConfirmPassword("differentPassword");
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authenticationService.register(registrationRequest));

        assertEquals(ErrorCode.PASSWORD_MISMATCH, exception.getErrorCode());
        verify(userRepository).findByEmailIgnoreCase(registrationRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void refreshToken_Success() {
        RefreshRequest refreshRequest = RefreshRequest.builder()
                .refreshToken("refreshToken")
                .build();

        when(jwtService.refreshToken(refreshRequest.getRefreshToken())).thenReturn("newAccessToken");

        AuthenticationResponse response = authenticationService.refreshToken(refreshRequest);


        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());

        verify(jwtService).refreshToken(refreshRequest.getRefreshToken());
    }

    @Test
    void logout_Success() {

        String userId = "user123";

        authenticationService.logout(userId);

        verify(jwtService).dropAllTokens(userId);
    }
}