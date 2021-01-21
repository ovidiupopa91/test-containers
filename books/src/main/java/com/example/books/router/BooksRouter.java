package com.example.books.router;

import com.example.books.handler.GetBookHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class BooksRouter {

    @Bean
    public RouterFunction<ServerResponse> route(final GetBookHandler handler) {
        return RouterFunctions.route(GET("/book"), handler::handleRequest);
    }
}
