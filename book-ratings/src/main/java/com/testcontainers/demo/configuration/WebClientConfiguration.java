package com.testcontainers.demo.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {
    @Value("${books.base.url}")
    private String bookUrl;

    @Value("${books.path}")
    private String bookPath;

    @Bean
    WebClient bookWebClient() {
        return WebClient.builder().baseUrl(bookUrl + bookPath).build();
    }
}
