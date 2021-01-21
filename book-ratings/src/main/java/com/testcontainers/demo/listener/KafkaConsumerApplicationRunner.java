package com.testcontainers.demo.listener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@AllArgsConstructor
@Slf4j
public class KafkaConsumerApplicationRunner implements ApplicationRunner  {

    private final KafkaConsumer kafkaConsumer;
    private final ReactiveKafkaConsumerTemplate<String, String> consumerTemplate;

    /**
     * Creates consumer receive flux instance
     *
     * @return consumer flux
     */
    public Flux<ReceiverRecord<String, String>> receive() {
        return Flux.defer(consumerTemplate::receive)
                .groupBy(m -> m.receiverOffset().topicPartition())
                .flatMap(kafkaConsumer::onSubmissionKafkaMessage)
                .doOnError(e -> log.error("Kafka Consumer error", e))

                // retry with backoff policy
                .retryWhen(Retry.backoff(3,Duration.ofSeconds(1)))
                // for now cycle in retries, if we let exception to bubble up it shuts down the consumer (Flux is terminated)
                .retry();
    }


    @Override
    public void run(ApplicationArguments args) {
        receive().subscribe();
    }
}