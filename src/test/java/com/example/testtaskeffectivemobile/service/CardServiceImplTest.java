package com.example.testtaskeffectivemobile.service;



import com.example.testtaskeffectivemobile.dto.request.CardCreateRequest;
import com.example.testtaskeffectivemobile.dto.request.TransferRequest;
import com.example.testtaskeffectivemobile.dto.response.CardDTO;
import com.example.testtaskeffectivemobile.entity.Card;
import com.example.testtaskeffectivemobile.entity.CardStatus;
import com.example.testtaskeffectivemobile.entity.User;
import com.example.testtaskeffectivemobile.exception.BusinessException;
import com.example.testtaskeffectivemobile.exception.ErrorCode;
import com.example.testtaskeffectivemobile.mapper.CardMapper;
import com.example.testtaskeffectivemobile.repository.CardRepository;
import com.example.testtaskeffectivemobile.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardServiceImpl cardService;

    private User user;
    private User adminUser;
    private Card card;
    private CardDTO cardDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("user123")
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .enabled(true)
                .locked(false)
                .build();

        adminUser = User.builder()
                .id("admin123")
                .email("admin@example.com")
                .firstName("Admin")
                .lastName("User")
                .enabled(true)
                .locked(false)
                .build();

        card = Card.builder()
                .id("card123")
                .cardNumber("1234567812345678")
                .activationDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusYears(3))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .user(user)
                .build();

        cardDTO = CardDTO.builder()
                .id("card123")
                .cardNumber("**** **** **** 5678")
                .activationDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusYears(3))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .userId("user123")
                .build();
    }

    @Test
    void createCard_Success() {
        // Arrange
        CardCreateRequest request = CardCreateRequest.builder()
                .userId("user123")
                .initialBalance(new BigDecimal("500.00"))
                .build();

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.toDTO(any(Card.class))).thenReturn(cardDTO);

        // Act
        CardDTO result = cardService.createCard(request);

        // Assert
        assertNotNull(result);
        assertEquals("card123", result.getId());
        verify(userRepository).findById("user123");
        verify(cardRepository).save(any(Card.class));
        verify(cardMapper).toDTO(any(Card.class));
    }

    @Test
    void createCard_UserNotFound_ThrowsBusinessException() {

        CardCreateRequest request = CardCreateRequest.builder()
                .userId("nonexistent")
                .initialBalance(new BigDecimal("500.00"))
                .build();

        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());


        BusinessException exception = assertThrows(BusinessException.class,
                () -> cardService.createCard(request));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository).findById("nonexistent");
        verify(cardRepository, never()).save(any(Card.class));
    }


    @Test
    void blockCard_CardNotFound_ThrowsBusinessException() {
        // Arrange
        when(cardRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> cardService.blockCard("nonexistent", "user123"));

        assertEquals(ErrorCode.CARD_NOT_FOUND, exception.getErrorCode());
        verify(cardRepository).findById("nonexistent");
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transfer_Success() {
        // Arrange
        Card fromCard = Card.builder()
                .id("fromCard123")
                .cardNumber("1111111111111111")
                .balance(new BigDecimal("1000.00"))
                .status(CardStatus.ACTIVE)
                .user(user)
                .build();

        Card toCard = Card.builder()
                .id("toCard123")
                .cardNumber("2222222222222222")
                .balance(new BigDecimal("500.00"))
                .status(CardStatus.ACTIVE)
                .user(user)
                .build();

        TransferRequest request = TransferRequest.builder()
                .fromCardId("fromCard123")
                .toCardId("toCard123")
                .amount(new BigDecimal("300.00"))
                .build();

        when(cardRepository.findById("fromCard123")).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById("toCard123")).thenReturn(Optional.of(toCard));
        when(cardRepository.save(any(Card.class))).thenReturn(fromCard, toCard);

        // Act
        cardService.transfer(request, "user123");

        // Assert
        assertEquals(new BigDecimal("700.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("800.00"), toCard.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transfer_InsufficientFunds_ThrowsBusinessException() {
        // Arrange
        Card fromCard = Card.builder()
                .id("fromCard123")
                .cardNumber("1111111111111111")
                .balance(new BigDecimal("100.00"))
                .status(CardStatus.ACTIVE)
                .user(user)
                .build();

        Card toCard = Card.builder()
                .id("toCard123")
                .cardNumber("2222222222222222")
                .balance(new BigDecimal("500.00"))
                .status(CardStatus.ACTIVE)
                .user(user)
                .build();

        TransferRequest request = TransferRequest.builder()
                .fromCardId("fromCard123")
                .toCardId("toCard123")
                .amount(new BigDecimal("300.00"))
                .build();

        when(cardRepository.findById("fromCard123")).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById("toCard123")).thenReturn(Optional.of(toCard));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> cardService.transfer(request, "user123"));

        assertEquals(ErrorCode.INSUFFICIENT_FUNDS, exception.getErrorCode());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void getUserCardsForTransfer_Success() {
        // Arrange
        when(cardRepository.findByUserIdAndStatus("user123", CardStatus.ACTIVE))
                .thenReturn(List.of(card));
        when(cardMapper.toDTO(any(Card.class))).thenReturn(cardDTO);

        // Act
        List<CardDTO> result = cardService.getUserCardsForTransfer("user123");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("card123", result.get(0).getId());
        verify(cardRepository).findByUserIdAndStatus("user123", CardStatus.ACTIVE);
        verify(cardMapper).toDTO(card);
    }

    @Test
    void getUserCardsByUserId_Success() {
        // Arrange
        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(cardRepository.findByUserId("user123")).thenReturn(List.of(card));
        when(cardMapper.toDTO(any(Card.class))).thenReturn(cardDTO);

        // Act
        List<CardDTO> result = cardService.getUserCardsByUserId("user123");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findById("user123");
        verify(cardRepository).findByUserId("user123");
        verify(cardMapper).toDTO(card);
    }

    @Test
    void getUserCardsByUserId_UserNotFound_ThrowsBusinessException() {
        // Arrange
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> cardService.getUserCardsByUserId("nonexistent"));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository).findById("nonexistent");
        verify(cardRepository, never()).findByUserId(anyString());
    }
}