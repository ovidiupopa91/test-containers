package com.example.books.api.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
        "spring.datasource.url=jdbc:tc:mysql:8.0.20:///databaseName?TC_INITSCRIPT=file:src/test/resources/schema.sql",
                "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
        }
)
class BooksApplicationTests {

    @LocalServerPort
    protected int port;

    private WebTestClient webTestClient;

    @BeforeEach
    public void setup() {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @AfterEach
    public void tearDown() {
        webTestClient = null;
    }


    @Test
    void contextLoads() {
    }

    @Test
    void getBook(){
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("book").queryParam("bookId", "Spring in Action").build())
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void getUnknownBook(){
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("book").queryParam("bookId", "Spring in Action2").build())
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    public void healthCheck() {
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus()
                .isOk();
    }

}
