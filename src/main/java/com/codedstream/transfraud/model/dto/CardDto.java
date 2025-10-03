package com.codedstream.transfraud.model.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardDto {
    private String id;
    private String cardNumber;
    private String cardHolderName;
    private LocalDate expiryDate;
    private String cardType;
    private Double creditLimit;
    private Double availableBalance;
    private Boolean isActive;
    private String customerId;
}
