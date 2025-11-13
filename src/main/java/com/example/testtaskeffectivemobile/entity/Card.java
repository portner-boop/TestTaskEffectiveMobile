package com.example.testtaskeffectivemobile.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "CARDS")
public class Card extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "CARD_NUMBER",nullable = false)
    private String cardNumber;

    @Column(name = "ACTIVATION_DATE",nullable = false)
    private LocalDate activationDate;

    @Column(name = "EXPIRATION_DATE",nullable = false)
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS",nullable = false)
    private CardStatus status;

    @Column(name = "BALANCE",nullable = false)
    private BigDecimal balance;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;
}
