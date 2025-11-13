package com.example.testtaskeffectivemobile.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferRequest {
    private String fromCardId;
    private String toCardId;
    private BigDecimal amount;
}
