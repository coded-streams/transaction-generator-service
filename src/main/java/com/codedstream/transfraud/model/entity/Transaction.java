package com.codedstream.transfraud.model.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    private String id;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String merchantId;

    @Column(nullable = false)
    private String merchantName;

    private String merchantCategory;

    @Embedded
    private MerchantLocation merchantLocation;

    @Column(nullable = false)
    private String transactionType;

    @Column(nullable = false)
    private Boolean isCardPresent;

    @Embedded
    private DeviceInfo deviceInfo;

    @Column(nullable = false)
    private LocalDateTime transactionTimestamp;

    @Column(nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MerchantLocation {
        private Double latitude;
        private Double longitude;
        private String city;
        private String country;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeviceInfo {
        private String deviceId;
        private String deviceType;
        private String ipAddress;
        private String userAgent;
    }
}
