package com.codedstream.transfraud.model.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    private String id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phoneNumber;

    @Embedded
    private Address address;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private Double averageTransactionAmount;

    private String typicalTransactionHours;

    @Builder.Default
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Card> cards = new ArrayList<>();

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Address {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private Double latitude;
        private Double longitude;
    }
}
