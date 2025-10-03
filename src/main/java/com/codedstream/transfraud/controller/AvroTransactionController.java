package com.codedstream.transfraud.controller;

import com.codedstream.transfraud.service.AvroTransactionGeneratorService;
import com.codedstream.transfraud.service.DataGeneratorService;
import com.codedstream.transfruad.library.schema.CardTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/avro-transactions")
@RequiredArgsConstructor
public class AvroTransactionController {

    private final AvroTransactionGeneratorService avroTransactionGeneratorService;
    private final DataGeneratorService dataGeneratorService;

    @PostMapping("/random")
    public ResponseEntity<Map<String, Object>> generateRandomAvroTransaction() {
        try {
            CardTransaction transaction = avroTransactionGeneratorService.generateRandomAvroTransaction();
            avroTransactionGeneratorService.generateAndSendRandomTransaction();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Random Avro transaction generated and sent to Kafka");
            response.put("transactionId", transaction.getTransactionId());
            response.put("cardId", transaction.getCardId());
            response.put("amount", transaction.getTransactionAmount());
            response.put("merchant", transaction.getMerchantName());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating random Avro transaction: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to generate Avro transaction: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> generateBulkAvroTransactions(
            @RequestParam(defaultValue = "10") int count) {

        if (count <= 0 || count > 1000) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Count must be between 1 and 1000");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            avroTransactionGeneratorService.generateAndSendMultipleTransactions(count);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Bulk Avro transactions generation completed");
            response.put("requestedCount", count);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating bulk Avro transactions: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to generate bulk transactions: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "Avro Transaction Generator");
        response.put("timestamp", System.currentTimeMillis());
        response.put("availableCards", dataGeneratorService.getActiveCardCount());
        response.put("totalCustomers", dataGeneratorService.getTotalCustomers());

        return ResponseEntity.ok(response);
    }
}
