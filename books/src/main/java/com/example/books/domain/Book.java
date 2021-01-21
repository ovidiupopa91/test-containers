package com.example.books.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Book {


    @Id
    private String bookId;

    @Column
    private String author;
}
