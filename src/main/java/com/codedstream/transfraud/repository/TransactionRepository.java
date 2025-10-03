package com.codedstream.transfraud.repository;

import com.codedstream.transfraud.model.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByCardIdOrderByTransactionTimestampDesc(String cardId);

    List<Transaction> findByTransactionTimestampBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.card.id = :cardId AND t.transactionTimestamp >= :since")
    Long countTransactionsSince(@Param("cardId") String cardId, @Param("since") LocalDateTime since);

    @Query("SELECT AVG(t.amount) FROM Transaction t WHERE t.card.customer.id = :customerId")
    Double findAverageTransactionAmountByCustomer(@Param("customerId") String customerId);

    @Query("SELECT t FROM Transaction t WHERE t.amount > :amountThreshold")
    List<Transaction> findLargeTransactions(@Param("amountThreshold") Double amountThreshold);
}
