package io.confluent.developer.movies.domain;


import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class Rating implements Serializable {

  private static final long serialVersionUID = 1L;

  public long movieId;
  public double rating;
  public long timestamp;  // milliseconds since epoch

  public Rating() {
    this(0, 0.0, System.currentTimeMillis());
  }

  @JsonCreator
  public Rating(
      @JsonProperty("movieId") long movieId,
      @JsonProperty("rating") double rating,
      @JsonProperty("timestamp") long timestamp) {
    this.movieId = movieId;
    this.rating = rating;
    this.timestamp = timestamp;
  }

  // Factory method if you need to create from Instant
  public static Rating fromInstant(long movieId, double rating, Instant instant) {
    return new Rating(movieId, rating, instant.toEpochMilli());
  }

  // Convert to Instant if needed
  public Instant convertTimestampAsInstant() {
    return Instant.ofEpochMilli(timestamp);
  }

  // Standard getters and setters
  public long getMovieId() {
    return movieId;
  }

  public void setMovieId(long movieId) {
    this.movieId = movieId;
  }

  public double getRating() {
    return rating;
  }

  public void setRating(double rating) {
    this.rating = rating;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public String toString() {
    return "Rating{" +
           "movieId=" + movieId +
           ", rating=" + rating +
           ", timestamp=" + Instant.ofEpochMilli(timestamp) +
           '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Rating rating1 = (Rating) o;
    return movieId == rating1.movieId &&
           Double.compare(rating1.rating, rating) == 0 &&
           timestamp == rating1.timestamp;
  }

  @Override
  public int hashCode() {
    return Objects.hash(movieId, rating, timestamp);
  }
}