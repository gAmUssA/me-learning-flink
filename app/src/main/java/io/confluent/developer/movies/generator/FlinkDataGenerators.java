package io.confluent.developer.movies.generator;


import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import io.confluent.developer.movies.config.JacksonConfig;
import io.confluent.developer.movies.domain.Movie;
import io.confluent.developer.movies.domain.Rating;

public class FlinkDataGenerators {

  private static final Logger LOG = LoggerFactory.getLogger(FlinkDataGenerators.class);

  // Static class to hold serializable movie generation function
  private static class MovieGeneratorFunction implements GeneratorFunction<Long, Movie>, Serializable {

    private static final long serialVersionUID = 1L;
    private final ArrayList<Movie> movies;

    public MovieGeneratorFunction(List<Movie> movies) {
      LOG.debug("Creating MovieGeneratorFunction with {} movies", movies.size());
      // Create new ArrayList and explicitly copy each movie to ensure proper serialization
      this.movies = new ArrayList<>();
      for (Movie movie : movies) {
        LOG.debug("Adding movie: {}", movie.getTitle());
        this.movies.add(new Movie(
            movie.getMovieId(),
            movie.getTitle(),
            movie.getReleaseYear(),
            movie.getCountry(),
            movie.getRating(),
            new ArrayList<>(movie.getGenres()),
            new ArrayList<>(movie.getActors()),
            new ArrayList<>(movie.getDirectors()),
            new ArrayList<>(movie.getComposers()),
            new ArrayList<>(movie.getScreenwriters()),
            new ArrayList<>(movie.getProductionCompanies()),
            movie.getCinematographer()
        ));
      }
      LOG.debug("MovieGeneratorFunction initialization complete");
    }

    @Override
    public Movie map(Long index) {
      if (index >= movies.size()) {
        return null;
      }
      Movie movie = movies.get(index.intValue());
      LOG.debug("Generating movie: {} (ID: {})", movie.getTitle(), movie.getMovieId());
      return movie;
    }
  }

  // Static class to hold serializable rating generation function
  private static class RatingGeneratorFunction implements GeneratorFunction<Long, Rating>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public Rating map(Long index) {
      return new RatingGenerator().generate();
    }
  }

  private final ArrayList<Movie> movies;

  public FlinkDataGenerators(String resourcePath) {
    LOG.debug("Loading movies from: {}", resourcePath);
    this.movies = (ArrayList<Movie>) loadMoviesFromClasspath(resourcePath);
    LOG.info("Loaded {} movies from resource: {}", movies.size(), resourcePath);
  }

  private List<Movie> loadMoviesFromClasspath(String resourcePath) {
    ObjectMapper objectMapper = JacksonConfig.createObjectMapper();
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
      if (inputStream == null) {
        throw new IllegalArgumentException(
            String.format("Resource not found in classpath: %s", resourcePath)
        );
      }

      ArrayList<Movie> loadedMovies = new ArrayList<>();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
        String line;
        int lineNumber = 0;
        while ((line = reader.readLine()) != null) {
          lineNumber++;
          if (!line.trim().isEmpty()) {
            try {
              Movie movie = objectMapper.readValue(line, Movie.class);
              LOG.debug("Loaded movie {} from line {}: {}",
                        movie.getMovieId(), lineNumber, movie.getTitle());
              loadedMovies.add(movie);
            } catch (Exception e) {
              LOG.error("Failed to parse movie at line {}: {}", lineNumber, line, e);
              throw new RuntimeException("Failed to parse movie at line " + lineNumber, e);
            }
          }
        }
      }
      LOG.debug("Successfully loaded {} movies", loadedMovies.size());
      return loadedMovies;
    } catch (Exception e) {
      LOG.error("Failed to load movies from resource: {}", resourcePath, e);
      throw new RuntimeException("Failed to load movies from resource: " + resourcePath, e);
    }
  }

  public DataGeneratorSource<Movie> createMovieSource() {
    if (movies.isEmpty()) {
      throw new IllegalStateException("No movies loaded. Cannot create movie source.");
    }

    LOG.debug("Creating movie source with {} movies", movies.size());
    return new DataGeneratorSource<>(
        new MovieGeneratorFunction(movies),
        movies.size(),
        TypeInformation.of(Movie.class)
    );
  }

  public DataGeneratorSource<Rating> createRatingSource(long numRatings, int ratingsPerSecond) {
    if (movies.isEmpty()) {
      throw new IllegalStateException("No movies loaded. Cannot create rating source.");
    }

    return new DataGeneratorSource<>(
        new RatingGeneratorFunction(),
        numRatings,
        RateLimiterStrategy.perSecond(ratingsPerSecond),
        TypeInformation.of(Rating.class)
    );
  }

  public DataGeneratorSource<Rating> createRatingSourceForMovie(
      long movieId,
      long numRatings,
      int ratingsPerSecond) {
    if (movies.isEmpty()) {
      throw new IllegalStateException("No movies loaded. Cannot create rating source.");
    }

    if (movieId < RatingGenerator.getMinMovieId() || movieId > RatingGenerator.getMaxMovieId()) {
      throw new IllegalArgumentException(
          String.format("Movie ID must be between %d and %d",
                        RatingGenerator.getMinMovieId(),
                        RatingGenerator.getMaxMovieId())
      );
    }

    return new DataGeneratorSource<>(
        new RatingGeneratorFunction(),
        numRatings,
        RateLimiterStrategy.perSecond(ratingsPerSecond),
        TypeInformation.of(Rating.class)
    );
  }

  public int getMovieCount() {
    return movies.size();
  }
}