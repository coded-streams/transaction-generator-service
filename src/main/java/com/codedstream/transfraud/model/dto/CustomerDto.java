package com.codedstream.transfraud.model.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private AddressDto address;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressDto {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private Double latitude;
        private Double longitude;
    }
}
