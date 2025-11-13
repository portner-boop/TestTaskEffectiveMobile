package com.example.testtaskeffectivemobile.controller;

import com.example.testtaskeffectivemobile.dto.request.CardCreateRequest;
import com.example.testtaskeffectivemobile.dto.request.CardFilterRequest;
import com.example.testtaskeffectivemobile.dto.request.TransferRequest;
import com.example.testtaskeffectivemobile.dto.response.CardDTO;
import com.example.testtaskeffectivemobile.entity.User;
import com.example.testtaskeffectivemobile.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "API для управления банковскими картами")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService cardService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Создать новую карту",
            description = "Только для администраторов. Создает новую банковскую карту для пользователя."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Карта успешно создана"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - требуется роль ADMIN"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса")
    })
    public CardDTO createCard(@RequestBody CardCreateRequest request) {
        return cardService.createCard(request);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Получить все карты",
            description = "Только для администраторов. Возвращает список всех карт в системе с возможностью фильтрации."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список карт успешно получен"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - требуется роль ADMIN")
    })
    public List<CardDTO> getAllCards(
            @Parameter(description = "Параметры фильтрации карт") CardFilterRequest filter) {
        return cardService.getAllCards(filter);
    }

    @PatchMapping("/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Заблокировать карту",
            description = "Только для администраторов. Блокирует указанную карту."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта успешно заблокирована"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - требуется роль ADMIN"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public CardDTO blockCard(
            @Parameter(description = "ID карты", required = true) @PathVariable String cardId,
            Authentication authentication) {
        String userId = getUserId(authentication);
        return cardService.blockCard(cardId, userId);
    }

    @PatchMapping("/{cardId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Активировать карту",
            description = "Только для администраторов. Активирует ранее заблокированную карту."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта успешно активирована"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - требуется роль ADMIN"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public CardDTO activateCard(
            @Parameter(description = "ID карты", required = true) @PathVariable String cardId,
            Authentication authentication) {
        String userId = getUserId(authentication);
        return cardService.activateCard(cardId, userId);
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Удалить карту",
            description = "Только для администраторов. Удаляет карту из системы."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Карта успешно удалена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - требуется роль ADMIN"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public void deleteCard(
            @Parameter(description = "ID карты", required = true) @PathVariable String cardId) {
        cardService.deleteCard(cardId);
    }

    @GetMapping("/my")
    @Operation(
            summary = "Получить мои карты",
            description = "Для всех авторизованных пользователей. Возвращает карты текущего пользователя."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список карт успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public List<CardDTO> getMyCards(
            @Parameter(description = "Параметры фильтрации") CardFilterRequest filter,
            Authentication authentication) {
        String userId = getUserId(authentication);
        return cardService.getUserCards(filter, userId);
    }

    @GetMapping("/{cardId}")
    @Operation(
            summary = "Получить информацию о карте",
            description = "Для всех авторизованных пользователей. Возвращает информацию о конкретной карте (только свои карты или все для администратора)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Информация о карте успешно получена"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к этой карте"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public CardDTO getCard(
            @Parameter(description = "ID карты", required = true) @PathVariable String cardId,
            Authentication authentication) {
        String userId = getUserId(authentication);
        return cardService.getCardById(cardId, userId);
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Перевод между картами",
            description = "Для всех авторизованных пользователей. Перевод средств между своими картами."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Перевод успешно выполнен"),
            @ApiResponse(responseCode = "400", description = "Недостаточно средств или неверные данные"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к указанным картам")
    })
    public void transfer(
            @RequestBody TransferRequest request,
            Authentication authentication) {
        String userId = getUserId(authentication);
        cardService.transfer(request, userId);
    }

    @GetMapping("/my/for-transfer")
    @Operation(
            summary = "Получить карты для перевода",
            description = "Для всех авторизованных пользователей. Возвращает список активных карт пользователя, доступных для перевода."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список карт успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public List<CardDTO> getMyCardsForTransfer(Authentication authentication) {
        String userId = getUserId(authentication);
        return cardService.getUserCardsForTransfer(userId);
    }

    @PatchMapping("/my/{cardId}/block-request")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Запрос на блокировку карты",
            description = "Для всех авторизованных пользователей. Пользователь может запросить блокировку своей карты."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Запрос на блокировку отправлен"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к этой карте"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public void requestBlockCard(
            @Parameter(description = "ID карты", required = true) @PathVariable String cardId,
            Authentication authentication) {
        String userId = getUserId(authentication);
        cardService.blockCard(cardId, userId);
    }

    @GetMapping("/{userId}/cards")
    @Operation(
            summary = "Получить карты пользователя",
            description = "Для всех авторизованных пользователей. Возвращает карты указанного пользователя."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список карт успешно получен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<List<CardDTO>> getUserCards(
            @Parameter(description = "ID пользователя", required = true) @PathVariable String userId) {
        List<CardDTO> cards = cardService.getUserCardsByUserId(userId);
        return ResponseEntity.ok(cards);
    }

    private String getUserId(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getId();
    }
}