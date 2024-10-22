package io.confluent.developer.movies.domain;



import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import io.confluent.developer.movies.config.JacksonConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class MovieTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = JacksonConfig.createObjectMapper();
  }

  @Test
  void shouldCorrectlySerializeAndDeserialize() throws Exception {
    // Given
    Movie
        originalMovie =
        new Movie(1L, "Once Upon a Time in the West", 1968, "Italy", 8.2, List.of("Western"), List.of("Claudia Cardinale", "Charles Bronson"), List.of("Sergio Leone"), List.of("Ennio Morricone"),
                  List.of("Sergio Leone", "Sergio Donati"), List.of("Paramount Pictures"), "Tonino Delli Colli");

    // When
    String json = objectMapper.writeValueAsString(originalMovie);
    Movie deserializedMovie = objectMapper.readValue(json, Movie.class);

    // Then
    assertThat(deserializedMovie).usingRecursiveComparison().isEqualTo(originalMovie);
  }

  @Test
  void shouldHandleNullValues() throws Exception {
    // Given
    String jsonWithNulls = """
        {
            "movieId": 1,
            "title": null,
            "releaseYear": 2020,
            "country": null,
            "rating": 8.0,
            "genres": null,
            "actors": null,
            "directors": null,
            "composers": null,
            "screenwriters": null,
            "productionCompanies": null,
            "cinematographer": null
        }
        """;

    // When
    Movie movie = objectMapper.readValue(jsonWithNulls, Movie.class);

    // Then
    assertThat(movie).isNotNull();
    assertThat(movie.getMovieId()).isEqualTo(1);
    final String title = movie.getTitle();
    assertThat(title).isEmpty();
    assertThat(movie.getReleaseYear()).isEqualTo(2020);
    assertThat(movie.getCountry()).isEmpty();
    assertThat(movie.getRating()).isEqualTo(8.0);
    assertThat(movie.getGenres()).isEmpty();
    assertThat(movie.getActors()).isEmpty();
    assertThat(movie.getDirectors()).isEmpty();
    assertThat(movie.getComposers()).isEmpty();
    assertThat(movie.getScreenwriters()).isEmpty();
    assertThat(movie.getProductionCompanies()).isEmpty();
    assertThat(movie.getCinematographer()).isEmpty();
  }

  @Test
  void shouldCreateValidMovieWithDefaultConstructor() {
    // When
    Movie movie = new Movie();

    // Then
    assertThat(movie).isNotNull();
    assertThat(movie.getMovieId()).isZero();
    assertThat(movie.getTitle()).isEmpty();
    assertThat(movie.getReleaseYear()).isZero();
    assertThat(movie.getCountry()).isEmpty();
    assertThat(movie.getRating()).isZero();
    assertThat(movie.getGenres()).isEmpty();
    assertThat(movie.getActors()).isEmpty();
    assertThat(movie.getDirectors()).isEmpty();
    assertThat(movie.getComposers()).isEmpty();
    assertThat(movie.getScreenwriters()).isEmpty();
    assertThat(movie.getProductionCompanies()).isEmpty();
    assertThat(movie.getCinematographer()).isEmpty();
  }

  private static Stream<Arguments> provideMoviesFromJsonl() throws IOException {
    Path path = Paths.get("src/main/resources/movies.jsonl");
    List<String> lines = Files.readAllLines(path);

    return lines.stream()
        .map(line -> Arguments.of(line, String.format("Line %d", lines.indexOf(line) + 1)));
  }

  @ParameterizedTest(name = "Movie from {1}" )
  @MethodSource("provideMoviesFromJsonl")
  void shouldSerializeAndDeserializeMovieFromJsonl(String jsonLine, String lineInfo) {
    assertThatCode(() -> {
      // First, deserialize from JSON string to Movie object
      Movie movie = objectMapper.readValue(jsonLine, Movie.class);

      // Verify the movie object is valid
      assertThat(movie)
          .as("Movie object from %s", lineInfo)
          .satisfies(m -> {
            assertThat(m.getMovieId()).isGreaterThan(0);
            assertThat(m.getTitle()).isNotEmpty();
            assertThat(m.getReleaseYear()).isBetween(1800, 2030);
            assertThat(m.getRating()).isBetween(0.0, 10.0);

            // Verify collections are not null
            assertThat(m.getGenres()).isNotNull();
            assertThat(m.getActors()).isNotNull();
            assertThat(m.getDirectors()).isNotNull();
            assertThat(m.getComposers()).isNotNull();
            assertThat(m.getScreenwriters()).isNotNull();
            assertThat(m.getProductionCompanies()).isNotNull();
          });

      // Serialize back to JSON
      String serialized = objectMapper.writeValueAsString(movie);

      // Deserialize again to verify round-trip
      Movie deserializedMovie = objectMapper.readValue(serialized, Movie.class);

      // Verify the objects are equal after round-trip
      assertThat(deserializedMovie)
          .as("Round-trip serialization for %s", lineInfo)
          .usingRecursiveComparison()
          .isEqualTo(movie);

    }).as("Processing %s", lineInfo)
        .doesNotThrowAnyException();
  }

  @Test
  void shouldLoadMoviesJsonlFile() {
    assertThatCode(() -> {
      Path path = Paths.get("src/main/resources/movies.jsonl");
      assertThat(path)
          .exists()
          .isRegularFile()
          .isReadable();

      List<String> lines = Files.readAllLines(path);
      assertThat(lines)
          .isNotEmpty()
          .allSatisfy(line -> {
            assertThat(line)
                .isNotEmpty()
                .startsWith("{")
                .endsWith("}");
          });
    }).doesNotThrowAnyException();
  }
}