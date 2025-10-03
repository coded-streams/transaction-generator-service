package com.codedstream.transfraud.service;

import com.codedstream.transfraud.model.entity.Transaction;
import com.codedstream.transfruad.library.schema.CardTransaction;
import com.codedstream.transfruad.library.schema.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, CardTransaction> kafkaTemplate;

    @Value("${app.kafka.topics.transactions}")
    private String transactionsTopic;

    public void sendTransaction(CardTransaction cardTransaction) {
        try {
            ListenableFuture<SendResult<String, CardTransaction>> future =
                    kafkaTemplate.send(transactionsTopic, (String) cardTransaction.getTransactionId(), cardTransaction);

            future.addCallback(new ListenableFutureCallback<SendResult<String, CardTransaction>>() {
                @Override
                public void onSuccess(SendResult<String, CardTransaction> result) {
                    log.debug("Successfully sent transaction {} to topic {}",
                            cardTransaction.getTransactionId(), transactionsTopic);
                }

                @Override
                public void onFailure(Throwable ex) {
                    log.error("Failed to send transaction {} to topic {}",
                            cardTransaction.getTransactionId(), transactionsTopic, ex);
                }
            });
        } catch (Exception e) {
            log.error("Error sending transaction to Kafka: {}", e.getMessage(), e);
        }
    }

    // Lambda version (more concise)
    public void sendTransactionWithLambda(CardTransaction cardTransaction) {
        try {
            kafkaTemplate.send(transactionsTopic, (String) cardTransaction.getTransactionId(), cardTransaction)
                    .addCallback(
                            result -> log.debug("Successfully sent transaction {} to topic {}",
                                    cardTransaction.getTransactionId(), transactionsTopic),
                            ex -> log.error("Failed to send transaction {} to topic {}",
                                    cardTransaction.getTransactionId(), transactionsTopic, ex)
                    );
        } catch (Exception e) {
            log.error("Error sending transaction to Kafka: {}", e.getMessage(), e);
        }
    }

    public void sendTransaction(Transaction transaction) {
        try {
            CardTransaction cardTransaction = convertToAvro(transaction);
            sendTransaction(cardTransaction);
        } catch (Exception e) {
            log.error("Error converting and sending transaction: {}", e.getMessage(), e);
        }
    }

    private CardTransaction convertToAvro(Transaction transaction) {
        return CardTransaction.newBuilder()
                .setTransactionId(transaction.getId())
                .setCardId(transaction.getCard().getId())
                .setCustomerId(transaction.getCard().getCustomer().getId())
                .setTransactionAmount(transaction.getAmount())
                .setCurrency(transaction.getCurrency())
                .setMerchantId(transaction.getMerchantId())
                .setMerchantName(transaction.getMerchantName())
                .setMerchantCategory(transaction.getMerchantCategory())
                .setTransactionType(TransactionType.valueOf(transaction.getTransactionType()))
                .setIsCardPresent(transaction.getIsCardPresent())
                .setTransactionTimestamp(transaction.getTransactionTimestamp().toInstant(java.time.ZoneOffset.UTC).toEpochMilli())
                .build();
    }
}