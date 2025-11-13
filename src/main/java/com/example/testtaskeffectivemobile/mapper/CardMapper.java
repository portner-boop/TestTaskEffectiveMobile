package com.example.testtaskeffectivemobile.mapper;

import com.example.testtaskeffectivemobile.dto.response.CardDTO;
import com.example.testtaskeffectivemobile.entity.Card;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

    public CardDTO toDTO(Card card) {
        if (card == null) {
            return null;
        }
        return CardDTO.builder()
                .id(card.getId())
                .cardNumber(maskCardNumber(card.getCardNumber()))
                .activationDate(card.getActivationDate())
                .expirationDate(card.getExpirationDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .userId(card.getUser().getId())
                .build();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return cardNumber;
        }
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** ****" + lastFour;
    }
}