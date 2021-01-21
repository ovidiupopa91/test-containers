package com.testcontainers.demo.repositories;

import com.testcontainers.demo.domain.BookRating;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;


public interface RatingsRepository extends ReactiveMongoRepository<BookRating, String> {
}
