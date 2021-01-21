package com.testcontainers.demo.configuration;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class KafkaConfiguration {
    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ReactiveKafkaProducerTemplate<String, String> producerTemplate() {
        Map<String, Object> properties = new HashMap<>();
        addDefaultProperties(properties);
        addProducerProperties(properties);

        return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(properties));
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, String> consumerTemplate() {
        Map<String, Object> properties = new HashMap<>();
        addConsumerProperties(properties);
        List<String> topics = List.of("books");
        return new ReactiveKafkaConsumerTemplate<>(ReceiverOptions.<String, String>create(properties).subscription(topics));
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {

        Map<String, Object> properties = new HashMap<>();
        addDefaultProperties(properties);

        return new KafkaAdmin(properties);
    }

    @Bean
    NewTopic newTopic(){
        return new NewTopic("books",1,(short)1);
    }

    private void addProducerProperties(Map<String, Object> properties) {
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
    }

    private void addDefaultProperties(Map<String, Object> properties) {
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "books-producer");

    }

    private void addConsumerProperties(Map<String, Object> properties) {
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "books-group");
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "books-consumer");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
    }
}