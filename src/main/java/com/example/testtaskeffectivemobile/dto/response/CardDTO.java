package com.example.testtaskeffectivemobile.dto.response;

import com.example.testtaskeffectivemobile.entity.CardStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardDTO {
    private String id;
    private String cardNumber;
    private LocalDate activationDate;
    private LocalDate expirationDate;
    private CardStatus status;
    private BigDecimal balance;
    private String userId;
}