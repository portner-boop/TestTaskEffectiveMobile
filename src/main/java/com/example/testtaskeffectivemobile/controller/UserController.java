package com.example.testtaskeffectivemobile.controller;

import com.example.testtaskeffectivemobile.dto.request.ChangePasswordRequest;
import com.example.testtaskeffectivemobile.dto.request.ProfileUpdateRequest;
import com.example.testtaskeffectivemobile.dto.response.UserResponse;
import com.example.testtaskeffectivemobile.entity.User;
import com.example.testtaskeffectivemobile.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API для управления пользователями")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @PatchMapping("/me")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Обновить профиль",
            description = "Для всех авторизованных пользователей. Обновляет информацию профиля текущего пользователя."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Профиль успешно обновлен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public void updateProfile(
            @RequestBody final ProfileUpdateRequest request,
            final Authentication principal
    ) {
        userService.updateProfileInfo(request, getUserId(principal));
    }

    @PostMapping("/me/password")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Сменить пароль",
            description = "Для всех авторизованных пользователей. Изменяет пароль текущего пользователя."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Пароль успешно изменен"),
            @ApiResponse(responseCode = "400", description = "Неверный текущий пароль или пароли не совпадают"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public void changePassword(
            @RequestBody final ChangePasswordRequest request,
            final Authentication principal
    ) {
        userService.changePassword(request, getUserId(principal));
    }

    @PatchMapping("/me/reactivate")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Реактивировать аккаунт",
            description = "Для всех авторизованных пользователей. Реактивирует деактивированный аккаунт."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Аккаунт успешно реактивирован"),
            @ApiResponse(responseCode = "400", description = "Аккаунт уже активен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public void reactiveAccount(final Authentication principal) {
        userService.reactivateAccount(getUserId(principal));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Получить активных пользователей",
            description = "Только для администраторов. Возвращает список всех активных пользователей в системе."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - требуется роль ADMIN")
    })
    public ResponseEntity<List<UserResponse>> getActiveUsers() {
        List<UserResponse> activeUsers = userService.getActiveUsers();
        return ResponseEntity.ok(activeUsers);
    }

    private String getUserId(Authentication principal) {
        return ((User) principal.getPrincipal()).getId();
    }
}