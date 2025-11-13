package com.example.testtaskeffectivemobile.dto.request;

import lombok.*;

import java.math.BigDecimal;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardCreateRequest {
    private String userId;
    private BigDecimal initialBalance;
}