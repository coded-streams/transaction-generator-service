package com.codedstream.transfraud.mapper;

import com.codedstream.transfraud.model.entity.Transaction;
import com.codedstream.transfruad.library.schema.CardTransaction;
import com.codedstream.transfruad.library.schema.DeviceInfo;
import com.codedstream.transfruad.library.schema.MerchantLocation;
import com.codedstream.transfruad.library.schema.TransactionType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;

@Component
public class AvroMapper {

    public CardTransaction toAvro(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return CardTransaction.newBuilder()
                .setTransactionId(transaction.getId())
                .setCardId(transaction.getCard().getId())
                .setCustomerId(transaction.getCard().getCustomer().getId())
                .setTransactionTimestamp(transaction.getTransactionTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setTransactionAmount(transaction.getAmount())
                .setCurrency(transaction.getCurrency())
                .setMerchantId(transaction.getMerchantId())
                .setMerchantName(transaction.getMerchantName())
                .setMerchantCategory(transaction.getMerchantCategory())
                .setMerchantLocation(createMerchantLocation(transaction))
                .setTransactionType(TransactionType.valueOf(transaction.getTransactionType()))
                .setDeviceInfo(createDeviceInfo(transaction))
                .setIsCardPresent(transaction.getIsCardPresent())
                .setPreviousTransactionId(null)
                .build();
    }

    private MerchantLocation createMerchantLocation(Transaction transaction) {
        if (transaction.getMerchantLocation() == null) {
            return null;
        }

        return MerchantLocation.newBuilder()
                .setLatitude(transaction.getMerchantLocation().getLatitude())
                .setLongitude(transaction.getMerchantLocation().getLongitude())
                .setCity(transaction.getMerchantLocation().getCity())
                .setCountry(transaction.getMerchantLocation().getCountry())
                .build();
    }

    private DeviceInfo createDeviceInfo(Transaction transaction) {
        if (transaction.getDeviceInfo() == null) {
            return null;
        }

        return DeviceInfo.newBuilder()
                .setDeviceId(transaction.getDeviceInfo().getDeviceId())
                .setDeviceType(transaction.getDeviceInfo().getDeviceType())
                .setIpAddress(transaction.getDeviceInfo().getIpAddress())
                .setUserAgent(transaction.getDeviceInfo().getUserAgent())
                .build();
    }
}
