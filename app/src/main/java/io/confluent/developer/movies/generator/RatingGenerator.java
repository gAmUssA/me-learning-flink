package io.confluent.developer.movies.generator;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

import io.confluent.developer.movies.domain.Rating;

public class RatingGenerator implements Serializable {

  private static final long serialVersionUID = 1L;
  private static final int MIN_MOVIE_ID = 1;
  private static final int MAX_MOVIE_ID = 920;
  private static final double[] RATING_WEIGHTS = {
      0.01, 0.02, 0.05, 0.10, 0.15, 0.20, 0.22, 0.15, 0.07, 0.03
  };

  public Rating generate() {
    return new Rating(
        generateMovieId(),
        generateRating(),
        System.currentTimeMillis()
    );
  }

  public Rating generateWithMovieId(long movieId) {
    if (movieId < MIN_MOVIE_ID || movieId > MAX_MOVIE_ID) {
      throw new IllegalArgumentException(
          String.format("Movie ID must be between %d and %d", MIN_MOVIE_ID, MAX_MOVIE_ID)
      );
    }

    return new Rating(
        movieId,
        generateRating(),
        System.currentTimeMillis()
    );
  }

  private long generateMovieId() {
    return ThreadLocalRandom.current().nextLong(MIN_MOVIE_ID, MAX_MOVIE_ID + 1);
  }

  private double generateRating() {
    double value = ThreadLocalRandom.current().nextDouble();
    double sum = 0;

    for (int i = 0; i < RATING_WEIGHTS.length; i++) {
      sum += RATING_WEIGHTS[i];
      if (value <= sum) {
        double baseRating = i + 1.0;
        double noise = ThreadLocalRandom.current().nextDouble() * 0.9;
        return Math.min(10.0, Math.round((baseRating + noise) * 10.0) / 10.0);
      }
    }

    return 7.0;
  }

  // Utility methods
  public static int getMinMovieId() {
    return MIN_MOVIE_ID;
  }

  public static int getMaxMovieId() {
    return MAX_MOVIE_ID;
  }
}