package com.testcontainers.demo.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Rating {
	private Integer rating;
	private String comment;

	public Rating setRating(Integer rating) {
		this.rating = rating;
		return this;
	}

	public Rating setComment(String comment) {
		this.comment = comment;
		return this;
	}
}
