package com.testcontainers.demo.handler;

import com.testcontainers.demo.domain.BookRating;
import com.testcontainers.demo.repositories.RatingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
public class GetRatingsHandler {

	private final RatingsRepository ratingsRepository;

	public Mono<ServerResponse> handlerRequest(ServerRequest request) {

		String bookdId = request.queryParam("bookId").orElse("");
		return ratingsRepository
				.findById(bookdId)
				.onErrorReturn(new BookRating())
				.flatMap(result -> ServerResponse.ok().bodyValue(result));
	}
}
