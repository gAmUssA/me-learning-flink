package io.confluent.developer.movies.domain;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RatedMovie implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Movie movie;
    private final double averageRating;

    public RatedMovie() {
        this(new Movie(), 0.0);
    }

    @JsonCreator
    public RatedMovie(
            @JsonProperty("movie") Movie movie,
            @JsonProperty("averageRating") double averageRating) {
        this.movie = movie != null ? movie : new Movie();
        this.averageRating = averageRating;
    }

    // Factory method to create from a Movie and average rating
    public static RatedMovie fromMovie(Movie movie, double averageRating) {
        return new RatedMovie(movie, averageRating);
    }

    // Getters
    public Movie getMovie() {
        return movie;
    }

    public double getAverageRating() {
        return averageRating;
    }

    // Convenience methods to access movie properties
    public long getMovieId() {
        return movie.getMovieId();
    }

    public String getTitle() {
        return movie.getTitle();
    }

    @Override
    public String toString() {
        return "RatedMovie{" +
                "movieId=" + movie.getMovieId() +
                ", title='" + movie.getTitle() + '\'' +
                ", averageRating=" + averageRating +
                '}';
    }
}
