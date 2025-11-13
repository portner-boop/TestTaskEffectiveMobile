package com.example.testtaskeffectivemobile.repository;

import com.example.testtaskeffectivemobile.entity.Card;
import com.example.testtaskeffectivemobile.entity.CardStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, String>, JpaSpecificationExecutor<Card> {

    List<Card> findByUserIdAndStatus(String userId, CardStatus status);
    List<Card> findByUserId(String userId);
    @Query("SELECT c FROM Card c WHERE c.user.id = :userId")
    List<Card> findCardsByUserId(@Param("userId") String userId);
}
