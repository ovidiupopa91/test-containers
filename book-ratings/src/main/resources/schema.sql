-- create database if not exists books;

drop table if exists ratings_events;

CREATE TABLE IF NOT EXISTS ratings_events(
  id SERIAL PRIMARY KEY ,
  book_id VARCHAR(255) NOT NULL,
  bookRating int,
  message VARCHAR(255) NOT NULL
);