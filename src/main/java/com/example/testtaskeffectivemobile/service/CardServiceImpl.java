package com.example.testtaskeffectivemobile.service;

import com.example.testtaskeffectivemobile.dto.request.CardCreateRequest;
import com.example.testtaskeffectivemobile.dto.request.CardFilterRequest;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;

    @Override
    @Transactional
    public CardDTO createCard(CardCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, request.getUserId()));

        String cardNumber = generateCardNumber();

        Card card = Card.builder()
                .cardNumber(cardNumber)
                .activationDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusYears(3))
                .status(CardStatus.ACTIVE)
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .user(user)
                .createdBy("SYSTEM")
                .lastModifiedBy("SYSTEM")
                .build();

        Card savedCard = cardRepository.save(card);
        log.info("Created card {} for user {}", cardNumber, user.getEmail());

        return cardMapper.toDTO(savedCard);
    }

    @Override
    @Transactional
    public CardDTO blockCard(String cardId, String currentUserId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND, cardId));
        if (!isAdmin(currentUserId) && !card.getUser().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new BusinessException(ErrorCode.CARD_ALREADY_BLOCKED);
        }
        card.setStatus(CardStatus.BLOCKED);
        Card savedCard = cardRepository.save(card);
        log.info("Card {} blocked by user {}", cardId, currentUserId);
        return cardMapper.toDTO(savedCard);
    }

    @Override
    @Transactional
    public CardDTO activateCard(String cardId, String currentUserId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND, cardId));
        if (!isAdmin(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.CARD_ALREADY_ACTIVE);
        }
        card.setStatus(CardStatus.ACTIVE);
        Card savedCard = cardRepository.save(card);
        log.info("Card {} activated by admin {}", cardId, currentUserId);
        return cardMapper.toDTO(savedCard);
    }

    @Override
    @Transactional
    public void deleteCard(String cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND, cardId));
        cardRepository.delete(card);
        log.info("Card {} deleted", cardId);
    }

    @Override
    public CardDTO getCardById(String cardId, String currentUserId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND, cardId));
        if (!isAdmin(currentUserId) && !card.getUser().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return cardMapper.toDTO(card);
    }

    @Override
    public List<CardDTO> getUserCards(CardFilterRequest filter, String currentUserId) {
        Specification<Card> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), currentUserId));
            if (filter.getCardNumber() != null && !filter.getCardNumber().isEmpty()) {
                predicates.add(cb.like(root.get("cardNumber"), "%" + filter.getCardNumber() + "%"));
            }
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return cardRepository.findAll(spec).stream()
                .map(cardMapper::toDTO)
                .toList();
    }

    @Override
    public List<CardDTO> getAllCards(CardFilterRequest filter) {
        Specification<Card> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getCardNumber() != null && !filter.getCardNumber().isEmpty()) {
                predicates.add(cb.like(root.get("cardNumber"), "%" + filter.getCardNumber() + "%"));
            }
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return cardRepository.findAll(spec).stream()
                .map(cardMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public void transfer(TransferRequest request, String currentUserId) {
        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND, request.getFromCardId()));
        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND, request.getToCardId()));
        if (!fromCard.getUser().getId().equals(currentUserId) ||
                !toCard.getUser().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.CARD_NOT_ACTIVE);
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.CARD_NOT_ACTIVE);
        }
        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_FUNDS);
        }
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT);
        }
        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));
        cardRepository.save(fromCard);
        cardRepository.save(toCard);
        log.info("Transfer completed: {} from card {} to card {}",
                request.getAmount(), request.getFromCardId(), request.getToCardId());
    }

    @Override
    public List<CardDTO> getUserCardsForTransfer(String userId) {
        List<Card> cards = cardRepository.findByUserIdAndStatus(userId, CardStatus.ACTIVE);
        return cards.stream()
                .map(cardMapper::toDTO)
                .toList();
    }

    @Override
    public List<CardDTO> getUserCardsByUserId(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND, userId);
                });
        if (!user.isEnabled()) {
            throw new BusinessException(ErrorCode.USER_ACCOUNT_DISABLED);
        }
        if (user.isLocked()) {
            throw new BusinessException(ErrorCode.USER_ACCOUNT_LOCKED);
        }
        List<Card> cards = cardRepository.findByUserId(userId);

        if (cards.isEmpty()) {
            log.info("No cards found for user {}", userId);
            return List.of();
        }

        List<Card> activeCards = cards.stream()
                .filter(card -> card.getStatus() == CardStatus.ACTIVE)
                .filter(card -> card.getExpirationDate().isAfter(LocalDate.now()))
                .toList();

        if (activeCards.isEmpty()) {
            log.info("No active cards found for user {}", userId);
            return List.of();
        }

        log.info("Found {} active cards for user {}", activeCards.size(), userId);
        return activeCards.stream()
                .map(cardMapper::toDTO)
                .toList();
    }

    private String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            cardNumber.append((int) (Math.random() * 10));
        }
        return cardNumber.toString();
    }

    private boolean isAdmin(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
    }
}