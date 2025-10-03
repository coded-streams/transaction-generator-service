package com.codedstream.transfraud.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topics.transactions}")
    private String transactionsTopic;

    @Value("${app.kafka.topics.fraud-alerts}")
    private String fraudAlertsTopic;

    @Bean
    public NewTopic transactionsTopic() {
        return TopicBuilder.name(transactionsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic fraudAlertsTopic() {
        return TopicBuilder.name(fraudAlertsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
