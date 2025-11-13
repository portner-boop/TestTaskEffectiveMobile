package com.example.testtaskeffectivemobile.dto.request;

import com.example.testtaskeffectivemobile.entity.CardStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardFilterRequest {
    private String cardNumber;
    private CardStatus status;
}