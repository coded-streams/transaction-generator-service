package com.codedstream.transfraud.repository;

import com.codedstream.transfraud.model.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {
    List<Card> findByCustomerId(String customerId);

    List<Card> findByIsActiveTrue();

    Optional<Card> findByCardNumber(String cardNumber);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.isActive = true")
    long countActiveCards();

    @Query("SELECT c FROM Card c WHERE c.availableBalance > :minBalance")
    List<Card> findCardsWithSufficientBalance(@Param("minBalance") Double minBalance);
}
