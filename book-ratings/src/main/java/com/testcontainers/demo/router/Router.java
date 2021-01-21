package com.testcontainers.demo.router;

import com.testcontainers.demo.handler.GetRatingsHandler;
import com.testcontainers.demo.handler.SaveRatingsHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration
public class Router {

    @Bean
    public RouterFunction<ServerResponse> route(final SaveRatingsHandler handler, GetRatingsHandler getRatingsHandler) {
        return RouterFunctions.route(POST("/book/ratings"), handler::handleRequest)
                .andRoute(GET("/book/ratings"),getRatingsHandler::handlerRequest);
    }
}
