package com.testcontainers.demo.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.testcontainers.demo.domain.BookRating;
import com.testcontainers.demo.repositories.RatingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

	private static final ObjectReader OBJECT_READER = new ObjectMapper()
			.readerFor(BookRating.class);

	private final RatingsRepository ratingsRepository;

	public Flux<ReceiverRecord<String, String>> onSubmissionKafkaMessage(Flux<ReceiverRecord<String, String>> receiverRecordFlux) {
		return receiverRecordFlux.flatMap(receiverRecord -> {
			log.info("Received records:{}", receiverRecord);
			return getRating(receiverRecord)
					.doOnError(scanResult -> receiverRecord.receiverOffset().acknowledge())

					.flatMap(rating -> ratingsRepository.findById(rating.getBookId())
							.switchIfEmpty(Mono.just(new BookRating(rating.getBookId())))
							.flatMap(dbBookRating -> {
								dbBookRating.addRating(rating.getRatings());
								return ratingsRepository.save(dbBookRating);
							})
							.doOnNext(scanResult -> receiverRecord.receiverOffset().acknowledge())
							.doOnError(t -> receiverRecord.receiverOffset().acknowledge())
							.then(Mono.just(receiverRecord)));
		});
	}

	private Mono<BookRating> getRating(ReceiverRecord<String, String> receiverRecord) {
		return Mono.fromCallable(() -> OBJECT_READER.<BookRating>readValue(receiverRecord.value()))
				.doOnError(e -> log.error("Parsing message failed", e))
				.onErrorMap(e -> new RuntimeException("Invalid message"));
	}

}
