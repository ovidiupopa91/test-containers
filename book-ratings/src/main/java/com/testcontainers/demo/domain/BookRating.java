package com.testcontainers.demo.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Document
@EqualsAndHashCode(exclude = {"ratings"})
public class BookRating {

	@Id
	private String bookId;
	private List<Rating> ratings;

	public BookRating(){}

	public BookRating(String bookId){
		this.bookId = bookId;
	}

	public void addRating(List<Rating> rating) {
		Objects.requireNonNull(rating);
		if (ratings == null) {
			ratings = new ArrayList<>();
		}
		ratings.addAll(rating);
	}



}
