package com.example.books.handler;

import com.example.books.domain.Book;
import com.example.books.repositories.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetBookHandler {


    private final BookRepository bookRepository;

    public Mono<ServerResponse> handleRequest(ServerRequest request) {


        return Mono.fromSupplier(() -> request.queryParam("bookId").orElseThrow(() -> new IllegalArgumentException("Book id is mandatory "))
        ).flatMap(bookId -> {
            log.info("Book id {}", bookId);
            Optional<Book> byId = bookRepository.findById(bookId);
            if (byId.isPresent()) {
                log.info("Returning ok");
                return ServerResponse.ok().build();
            }
            log.info("returning not found");
            return ServerResponse.notFound().build();
        });


    }
}
