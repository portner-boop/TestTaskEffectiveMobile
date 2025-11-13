package com.example.testtaskeffectivemobile.controller;

import com.example.testtaskeffectivemobile.dto.request.CardCreateRequest;
import com.example.testtaskeffectivemobile.dto.response.CardDTO;
import com.example.testtaskeffectivemobile.entity.CardStatus;
import com.example.testtaskeffectivemobile.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cardController).build();
        objectMapper = new ObjectMapper();
    }

    private CardDTO createCardDTO() {
        return CardDTO.builder()
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
    void createCard_Success() throws Exception {
        CardCreateRequest request = CardCreateRequest.builder()
                .userId("user123")
                .initialBalance(new BigDecimal("500.00"))
                .build();

        CardDTO response = createCardDTO();

        when(cardService.createCard(any(CardCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("card123"))
                .andExpect(jsonPath("$.balance").value(1000.00));

        verify(cardService).createCard(any(CardCreateRequest.class));
    }


    @Test
    void getAllCards_Success() throws Exception {
        CardDTO cardDTO = createCardDTO();
        when(cardService.getAllCards(any())).thenReturn(List.of(cardDTO));

        mockMvc.perform(get("/api/v1/cards/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("card123"));

        verify(cardService).getAllCards(any());
    }



    @Test
    void deleteCard_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/cards/card123"))
                .andExpect(status().isNoContent());

        verify(cardService).deleteCard("card123");
    }


    @Test
    void getUserCardsByUserId_Success() throws Exception {
        CardDTO cardDTO = createCardDTO();
        when(cardService.getUserCardsByUserId("user123")).thenReturn(List.of(cardDTO));

        mockMvc.perform(get("/api/v1/cards/user123/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("card123"));

        verify(cardService).getUserCardsByUserId("user123");
    }
}