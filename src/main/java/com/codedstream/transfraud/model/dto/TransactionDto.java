package com.codedstream.transfraud.model.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {
    private String id;
    private Double amount;
    private String currency;
    private String merchantId;
    private String merchantName;
    private String merchantCategory;
    private MerchantLocationDto merchantLocation;
    private String transactionType;
    private Boolean isCardPresent;
    private DeviceInfoDto deviceInfo;
    private LocalDateTime transactionTimestamp;
    private String status;
    private String cardId;
    private String customerId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MerchantLocationDto {
        private Double latitude;
        private Double longitude;
        private String city;
        private String country;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeviceInfoDto {
        private String deviceId;
        private String deviceType;
        private String ipAddress;
        private String userAgent;
    }
}
