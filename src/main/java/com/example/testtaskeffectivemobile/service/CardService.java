package com.example.testtaskeffectivemobile.service;

import com.example.testtaskeffectivemobile.dto.request.CardCreateRequest;
import com.example.testtaskeffectivemobile.dto.request.CardFilterRequest;
import com.example.testtaskeffectivemobile.dto.request.TransferRequest;
import com.example.testtaskeffectivemobile.dto.response.CardDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CardService {
    CardDTO createCard(CardCreateRequest request);
    CardDTO blockCard(String cardId, String currentUserId);
    CardDTO activateCard(String cardId, String currentUserId);
    void deleteCard(String cardId);
    CardDTO getCardById(String cardId, String currentUserId);
    List<CardDTO> getUserCards(CardFilterRequest filter, String currentUserId);
    List<CardDTO> getAllCards(CardFilterRequest filter);
    void transfer(TransferRequest request, String currentUserId);
    List<CardDTO> getUserCardsForTransfer(String userId);
    List<CardDTO> getUserCardsByUserId(String userId);
}