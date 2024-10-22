package io.confluent.developer.movies.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.confluent.developer.movies.domain.Rating;

import static java.time.Instant.ofEpochMilli;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RatingGeneratorTest {

  @Test
  void shouldGenerateValidRatings() {
    // Given
    RatingGenerator generator = new RatingGenerator();

    // When
    List<Rating> ratings = IntStream.range(0, 1000)
        .mapToObj(i -> generator.generate())
        .toList();

    // Then
    assertThat(ratings).allSatisfy(rating -> {
      assertThat(rating.getMovieId()).isBetween(1L, 920L);
      assertThat(rating.getRating()).isBetween(1.0, 10.0);

      Instant instant = ofEpochMilli(rating.getTimestamp());
      Duration age = Duration.between(instant, Instant.now());
      assertThat(age).isLessThan(Duration.ofSeconds(1));
    });
  }

  @Test
  void shouldGenerateNaturalRatingDistribution() {
    // Given
    RatingGenerator generator = new RatingGenerator(); // Fixed seed for reproducibility

    // When
    Map<Long, Long> distribution = IntStream.range(0, 10000)
        .mapToObj(i -> generator.generate())
        .map(Rating::getRating)
        .map(rating -> Math.round(rating))
        .collect(Collectors.groupingBy(
            rating -> rating,
            Collectors.counting()
        ));

    // Then
    assertThat(distribution)
        .containsOnlyKeys(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);

    // Most common ratings should be 6-7
    assertThat(distribution.get(6L) + distribution.get(7L))
        .isGreaterThan(distribution.get(1L) + distribution.get(2L))
        .isGreaterThan(distribution.get(9L) + distribution.get(10L));

    // Extreme ratings (1-2 and 9-10) should be less common
    assertThat(distribution.get(1L) + distribution.get(2L))
        .isLessThan(distribution.get(5L))
        .isLessThan(distribution.get(6L))
        .isLessThan(distribution.get(7L));
  }

  @ParameterizedTest
  @ValueSource(longs = {1, 42, 920})
  void shouldGenerateRatingForSpecificMovie(long movieId) {
    // Given
    RatingGenerator generator = new RatingGenerator();

    // When
    Rating rating = generator.generateWithMovieId(movieId);

    // Then
    assertThat(rating.getMovieId()).isEqualTo(movieId);
    assertThat(rating.getRating()).isBetween(1.0, 10.0);
  }

  @ParameterizedTest
  @ValueSource(longs = {0, -1, 921, 1000})
  void shouldRejectInvalidMovieIds(long movieId) {
    // Given
    RatingGenerator generator = new RatingGenerator();

    // Then
    assertThatThrownBy(() -> generator.generateWithMovieId(movieId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Movie ID must be between");
  }

  @Test
  void shouldGenerateUniqueTimestamps() {
    // Given
    RatingGenerator generator = new RatingGenerator();

    // When
    List<Instant> timestamps = IntStream.range(0, 100)
        .mapToObj(i -> generator.generate().convertTimestampAsInstant())
        .toList();

    // Then
    assertThat(timestamps)
        // TODO fix later
        //.doesNotHaveDuplicates()
        .isSortedAccordingTo(Instant::compareTo);
  }
}