package com.example.testtaskeffectivemobile.controller;

import com.example.testtaskeffectivemobile.dto.request.AuthenticationRequest;
import com.example.testtaskeffectivemobile.dto.request.RefreshRequest;
import com.example.testtaskeffectivemobile.dto.request.RegistrationRequest;
import com.example.testtaskeffectivemobile.dto.response.AuthenticationResponse;
import com.example.testtaskeffectivemobile.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для аутентификации и регистрации")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(
            summary = "Вход в систему",
            description = "Публичный endpoint. Аутентификация пользователя и получение JWT токенов."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация"),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
    })
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody @Valid final AuthenticationRequest authenticationRequest
    ) {
        return ResponseEntity.ok(authenticationService.login(authenticationRequest));
    }

    @GetMapping("/logout")
    @Operation(
            summary = "Выход из системы",
            description = "Для всех авторизованных пользователей. Завершает сеанс пользователя и удаляет токены."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный выход из системы"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<Void> logout(final Authentication principal) {
        this.authenticationService.logout(principal.getName());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/register")
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Публичный endpoint. Создает нового пользователя в системе."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Неверные данные регистрации или email уже существует")
    })
    public ResponseEntity<Void> register(
            @RequestBody @Valid final RegistrationRequest request
    ) {
        this.authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Обновление токена",
            description = "Публичный endpoint. Обновление access token с помощью refresh token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Токен успешно обновлен"),
            @ApiResponse(responseCode = "401", description = "Неверный или просроченный refresh token")
    })
    public ResponseEntity<AuthenticationResponse> refresh(
            @RequestBody @Valid final RefreshRequest request
    ) {
        return ResponseEntity.ok(this.authenticationService.refreshToken(request));
    }
}