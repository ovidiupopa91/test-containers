package com.testcontainers.demo.apitest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testcontainers.demo.domain.BookRating;
import com.testcontainers.demo.domain.Rating;
import org.assertj.core.matcher.AssertionMatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = BookRatingsApplicationTests.Initializer.class)
@Testcontainers(disabledWithoutDocker = true)
class BookRatingsApplicationTests {

	@Container
	static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka").withTag("5.4.3"));

	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest")
			.withExposedPorts(27017);

	@Container
	static MockServerContainer booksMockServerContainer = new MockServerContainer(DockerImageName.parse("jamesdbloom/mockserver").withTag("mockserver-5.10.0"));

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
	public void contextLoad() {

	}

	@Test
	void shouldPersistABookRating(){

		BookRating bookRating = new BookRating();
		bookRating.setBookId("test-book");
		Rating rating = new Rating();
		rating.setRating(4);
		bookRating.addRating(List.of(rating));

		MockServerClient mockServerClient = new MockServerClient(
				booksMockServerContainer.getContainerIpAddress(),
				booksMockServerContainer.getFirstMappedPort()

		);

		//test double
		mockServerClient.when(HttpRequest.request("/book").withQueryStringParameter("bookId","test-book"))
				.respond(HttpResponse.response().withStatusCode(200));

		webTestClient.post().uri(uriBuilder -> uriBuilder.path("/book/ratings").build())
				.bodyValue(bookRating)
				.exchange().expectStatus().isAccepted();

		await().untilAsserted(() -> {
			webTestClient.get().uri(uriBuilder -> uriBuilder.path("/book/ratings").queryParam("bookId","test-book").build())
					.exchange()
					.expectStatus()
					.isOk()
					.expectBody(BookRating.class)
					.value(new AssertionMatcher<BookRating>() {
						@Override
						public void assertion(BookRating actual) throws AssertionError {
							System.out.println("****** " + actual);
							assertThat(actual).isNotNull();
							assertThat(actual.getBookId()).isEqualTo("test-book");
							List<Rating> ratings = actual
									.getRatings();
							assertThat(ratings).isNotEmpty();
							Rating rating1 = ratings.get(0);

							assertThat(rating1.getRating()).isEqualTo(4);
						}
					});
		});

		//add a new raring for the same book
		BookRating secondBookRating = new BookRating();
		secondBookRating.setBookId("test-book");
		Rating secondRating = new Rating();
		secondRating.setRating(5);
		secondRating.setComment("very good");
		secondBookRating.addRating(List.of(secondRating));
		webTestClient.post().uri(uriBuilder -> uriBuilder.path("/book/ratings").build())
				.bodyValue(secondBookRating)
				.exchange().expectStatus().isAccepted();

		await().untilAsserted(() -> {
			webTestClient.get().uri(uriBuilder -> uriBuilder.path("/book/ratings").queryParam("bookId","test-book").build())
					.exchange()
					.expectStatus()
					.isOk()
					.expectBody(BookRating.class)
					.value(new AssertionMatcher<BookRating>() {
						@Override
						public void assertion(BookRating actual) throws AssertionError {
							System.out.println("*** " + actual);
							assertThat(actual).isNotNull();
							assertThat(actual.getBookId()).isEqualTo("test-book");
							List<Rating> ratings = actual
									.getRatings();
							assertThat(ratings).isNotEmpty().hasSize(2);
							assertThat(ratings).containsExactly(
									new Rating().setRating(4),
									new Rating().setRating(5).setComment("very good")
							);
						}
					});
		});

	}

	@Test
	public void healthCheck() {
		webTestClient.get().uri("/actuator/health")
				.exchange()
				.expectStatus()
				.isOk();
	}


	public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		@Override
		public void initialize(ConfigurableApplicationContext applicationContext) {
			TestPropertyValues.of(
					"kafka.bootstrap-servers=" + kafkaContainer.getBootstrapServers(),
					"books.base.url=" + booksMockServerContainer.getEndpoint(),
					"spring.data.mongodb.host=" + mongoDBContainer.getContainerIpAddress(),
					"spring.data.mongodb.port=" + mongoDBContainer.getFirstMappedPort()

			).applyTo(applicationContext);

		}
	}
}
