package io.confluent.developer.movies.domain;


import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RatingTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        .configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(new JavaTimeModule());
  }


  @Test
  void shouldSerializeAndDeserialize() throws Exception {
    // Given
    Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Rating originalRating = new Rating(42L, 8.5, now.toEpochMilli());

    // When
    String json = objectMapper.writeValueAsString(originalRating);
    Rating deserializedRating = objectMapper.readValue(json, Rating.class);

    // Then
    assertThat(deserializedRating)
        .usingRecursiveComparison()
        .isEqualTo(originalRating);
  }

  @Test
  void shouldHandleNullRatingTime() throws Exception {
    // Given
    String jsonWithNullTime = """
        {
            "movieId": 42,
            "rating": 8.5,
            "ratingTime": null
        }
        """;

    // When
    Rating rating = objectMapper.readValue(jsonWithNullTime, Rating.class);

    // Then
    assertThat(rating.getTimestamp()).isNotNull();
    assertThat(rating.getMovieId()).isEqualTo(42);
    assertThat(rating.getRating()).isEqualTo(8.5);
  }

  @ParameterizedTest
  @ValueSource(doubles = {0.0, 5.0, 10.0})
  void shouldHandleValidRatings(double ratingValue) throws Exception {
    // Given
    String json = String.format("""
                                    {
                                        "movieId": 42,
                                        "rating": %f,
                                        "ratingTime": "2024-01-01T00:00:00Z"
                                    }
                                    """, ratingValue);

    // When
    Rating rating = objectMapper.readValue(json, Rating.class);

    // Then
    assertThat(rating.getRating()).isEqualTo(ratingValue);
  }

  @Test
  void shouldCreateDefaultRating() {
    // When
    Rating rating = new Rating();

    // Then
    assertThat(rating.getMovieId()).isZero();
    assertThat(rating.getRating()).isZero();
    assertThat(rating.getTimestamp()).isNotNull();
  }

  private static Stream<Arguments> provideInvalidJson() {
    return Stream.of(
        Arguments.of(
            "{\"movieId\": \"not-a-number\", \"rating\": 8.5, \"timestamp\": 1704067200000}",
            InvalidFormatException.class,
            "Invalid movieId format"
        ),
        Arguments.of(
            "{\"movieId\": 42, \"rating\": \"not-a-number\", \"timestamp\": 1704067200000}",
            InvalidFormatException.class,
            "Invalid rating format"
        ),
        Arguments.of(
            "{\"movieId\": 42, \"rating\": 8.5, \"timestamp\": \"not-a-number\"}",
            InvalidFormatException.class,
            "Invalid timestamp format"
        )
    );
  }

  @ParameterizedTest(name = "should fail for {2}")
  @MethodSource("provideInvalidJson")
  void shouldHandleInvalidJson(String invalidJson, Class<? extends Exception> expectedExceptionClass, String testCase) {
    assertThatThrownBy(() -> objectMapper.readValue(invalidJson, Rating.class))
        .isInstanceOf(expectedExceptionClass);
  }

  @Test
  void shouldDeserializeValidJson() {
    assertThatCode(() -> {
      String json = "{\"movieId\": 42, \"rating\": 8.5, \"timestamp\": 1704067200000}";
      Rating rating = objectMapper.readValue(json, Rating.class);

      assertThat(rating.getMovieId()).isEqualTo(42);
      assertThat(rating.getRating()).isEqualTo(8.5);
      assertThat(rating.getTimestamp()).isEqualTo(1704067200000L);
    }).doesNotThrowAnyException();
  }

  @Test
  void shouldHandleMissingFields() throws Exception {
    // Given
    String incompleteJson = """
        {
            "movieId": 42
        }
        """;

    // When
    Rating rating = objectMapper.readValue(incompleteJson, Rating.class);

    // Then
    assertThat(rating.getMovieId()).isEqualTo(42);
    assertThat(rating.getRating()).isZero();
    assertThat(rating.getTimestamp()).isNotNull();
  }
}