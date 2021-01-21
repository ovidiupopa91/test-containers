package com.testcontainers.demo.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.testcontainers.demo.domain.BookRating;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SaveRatingsHandler {

	private static final ObjectWriter OBJECT_WRITER = new ObjectMapper()
			.writerFor(BookRating.class);

	private final ReactiveKafkaProducerTemplate<String, String> producerTemplate;
	private final WebClient bookWebClient;

	public Mono<ServerResponse> handleRequest(ServerRequest request) {

		return request.bodyToMono(BookRating.class)
				.flatMap(rating ->


						bookWebClient.get().uri(uriBuilder -> uriBuilder.queryParam("bookId", rating.getBookId()).build())
								.exchange()
								.flatMap(clientResponse -> {
									if (clientResponse.statusCode() == HttpStatus.OK) {
										return createProducerRecord(rating).flatMap(producerTemplate::send).then(ServerResponse.accepted().build());
									}
									return ServerResponse.notFound().build();
								}));


	}

	private Mono<ProducerRecord<String, String>> createProducerRecord(BookRating bookRating) {

		return Mono.fromCallable(() -> new ProducerRecord<>(
				// topic
				"books",
				// message key
				"book-key",
				// status update message
				OBJECT_WRITER.writeValueAsString(bookRating)
		));
	}
}
