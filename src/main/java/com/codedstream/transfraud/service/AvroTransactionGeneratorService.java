package com.codedstream.transfraud.service;

import com.codedstream.transfruad.library.schema.CardTransaction;
import com.codedstream.transfraud.model.entity.Card;
import com.codedstream.transfraud.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvroTransactionGeneratorService {

    private final CardRepository cardRepository;
    private final KafkaProducerService kafkaProducerService;
    //private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.redis.enabled:false}")
    private boolean redisEnabled;

    @Value("${app.avro.generator.max-bulk-size:1000}")
    private int maxBulkSize;

    private final Random random = new Random();
    private final String[] MERCHANT_NAMES = {
            "Amazon", "Walmart", "Starbucks", "Target", "Best Buy",
            "McDonald's", "Apple Store", "Netflix", "Uber", "Shell Gas"
    };
    private final String[] MERCHANT_CATEGORIES = {
            "RETAIL", "FOOD", "ENTERTAINMENT", "TRAVEL", "SERVICES", "UTILITIES"
    };
    private final String[] CITIES = {"New York", "Los Angeles", "Chicago", "Houston", "Miami"};
    private final String[] DEVICE_TYPES = {"MOBILE", "DESKTOP", "TABLET"};

    public CardTransaction generateRandomAvroTransaction() {
        List<Card> activeCards = cardRepository.findByIsActiveTrue();
        if (activeCards.isEmpty()) {
            throw new IllegalStateException("No active cards available for transaction generation");
        }

        Card randomCard = activeCards.get(random.nextInt(activeCards.size()));
        return createRandomAvroTransaction(randomCard);
    }

    public void generateAndSendRandomTransaction() {
        try {
            CardTransaction avroTransaction = generateRandomAvroTransaction();
            kafkaProducerService.sendTransaction(avroTransaction);

            // Only update Redis cache if enabled
//            if (redisEnabled) {
//                updateTransactionCache(avroTransaction);
//            }

            log.debug("Generated and sent Avro transaction: {}", avroTransaction.getTransactionId());
        } catch (Exception e) {
            log.error("Error generating Avro transaction: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Avro transaction", e);
        }
    }

    public void generateAndSendMultipleTransactions(int count) {
        if (count <= 0 || count > maxBulkSize) {
            throw new IllegalArgumentException("Count must be between 1 and " + maxBulkSize);
        }

        log.info("Generating {} random Avro transactions", count);
        int successCount = 0;

        for (int i = 0; i < count; i++) {
            try {
                CardTransaction avroTransaction = generateRandomAvroTransaction();
                kafkaProducerService.sendTransaction(avroTransaction);

                // Only update Redis cache if enabled
//                if (redisEnabled) {
//                    updateTransactionCache(avroTransaction);
//                }

                successCount++;

                // Small delay to avoid overwhelming the system
                if (i % 10 == 0) {
                    Thread.sleep(50);
                }
            } catch (Exception e) {
                log.error("Error generating transaction {} of {}: {}", i + 1, count, e.getMessage());
            }
        }

        log.info("Successfully generated {} out of {} Avro transactions", successCount, count);
    }

    private CardTransaction createRandomAvroTransaction(Card card) {
        String transactionId = UUID.randomUUID().toString();
        double amount = 10.0 + (random.nextDouble() * 490);
        boolean isOnline = random.nextDouble() < 0.4;
        String merchantCategory = MERCHANT_CATEGORIES[random.nextInt(MERCHANT_CATEGORIES.length)];

        com.codedstream.transfruad.library.schema.MerchantLocation merchantLocation =
                com.codedstream.transfruad.library.schema.MerchantLocation.newBuilder()
                        .setLatitude(34.0522 + (random.nextDouble() - 0.5) * 10)
                        .setLongitude(-118.2437 + (random.nextDouble() - 0.5) * 10)
                        .setCity(CITIES[random.nextInt(CITIES.length)])
                        .setCountry("USA")
                        .build();

        com.codedstream.transfruad.library.schema.DeviceInfo deviceInfo = null;
        if (isOnline) {
            deviceInfo = com.codedstream.transfruad.library.schema.DeviceInfo.newBuilder()
                    .setDeviceId("DEV_" + random.nextInt(10000))
                    .setDeviceType(DEVICE_TYPES[random.nextInt(DEVICE_TYPES.length)])
                    .setIpAddress(generateRandomIp())
                    .setUserAgent(generateRandomUserAgent())
                    .build();
        }

        return CardTransaction.newBuilder()
                .setTransactionId(transactionId)
                .setCardId(card.getId())
                .setCustomerId(card.getCustomer().getId())
                .setTransactionTimestamp(System.currentTimeMillis())
                .setTransactionAmount(amount)
                .setCurrency("USD")
                .setMerchantId("MERCH_" + random.nextInt(100000))
                .setMerchantName(MERCHANT_NAMES[random.nextInt(MERCHANT_NAMES.length)] + " " + random.nextInt(100))
                .setMerchantCategory(merchantCategory)
                .setMerchantLocation(merchantLocation)
                .setTransactionType(isOnline ? com.codedstream.transfruad.library.schema.TransactionType.ONLINE : com.codedstream.transfruad.library.schema.TransactionType.POS)
                .setDeviceInfo(deviceInfo)
                .setIsCardPresent(!isOnline)
                .setPreviousTransactionId(generatePreviousTransactionId(card.getId()))
                .build();
    }

//    private void updateTransactionCache(CardTransaction transaction) {
//        if (!redisEnabled) {
//            return;
//        }
//
//        try {
//            String key = "avro:transactions:" + transaction.getCardId();
//            redisTemplate.opsForList().leftPush(key, transaction.toString());
//            redisTemplate.expire(key, 1, TimeUnit.HOURS);
//
//            String countKey = "avro:transaction_count:" + transaction.getCardId();
//            redisTemplate.opsForValue().increment(countKey);
//            redisTemplate.expire(countKey, 1, TimeUnit.HOURS);
//
//            String timestampKey = "avro:last_transaction:" + transaction.getCardId();
//            redisTemplate.opsForValue().set(timestampKey, transaction.getTransactionTimestamp());
//            redisTemplate.expire(timestampKey, 1, TimeUnit.HOURS);
//        } catch (Exception e) {
//            log.warn("Redis cache update failed: {}", e.getMessage());
//            // Don't throw exception, just log the warning
//        }
//    }

    private String generateRandomIp() {
        return "192.168." + random.nextInt(256) + "." + random.nextInt(256);
    }

    private String generateRandomUserAgent() {
        String[] userAgents = {
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/537.36",
                "Mozilla/5.0 (Android 10; Mobile) AppleWebKit/537.36"
        };
        return userAgents[random.nextInt(userAgents.length)];
    }

    private String generatePreviousTransactionId(String cardId) {
        return random.nextDouble() < 0.7 ? null : "PREV_" + UUID.randomUUID().toString();
    }
}