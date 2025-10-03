package com.codedstream.transfraud.controller;

import com.codedstream.transfraud.service.DataGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/generator")
@RequiredArgsConstructor
public class DataGeneratorController {

    private final DataGeneratorService dataGeneratorService;

    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializeData() {
        dataGeneratorService.initializeSampleData();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Sample data initialization started");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCustomers", dataGeneratorService.getTotalCustomers());
        stats.put("activeCards", dataGeneratorService.getActiveCardCount());
        stats.put("totalTransactions", dataGeneratorService.getTotalTransactions());
        stats.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetData() {
        // Note: In a real application, you'd implement proper reset logic
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Reset endpoint - would clear data in production");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}
