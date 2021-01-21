create database if not exists book;

use book;

drop table if exists book;

CREATE TABLE IF NOT EXISTS book(
  book_id VARCHAR(255) NOT NULL PRIMARY KEY,
  author VARCHAR(255) NOT NULL
);



INSERT
  INTO book (book_id, author)
  VALUES ( 'Spring in Action', 'author');
