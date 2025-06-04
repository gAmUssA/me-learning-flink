package io.confluent.developer.movies.domain;

import java.io.Serializable;

/**
 * Class to hold the average rating for a movie.
 * Used as an intermediate result in the join operation.
 */
public class RatingAverage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final long movieId;
    private final double averageRating;
    
    public RatingAverage(long movieId, double averageRating) {
        this.movieId = movieId;
        this.averageRating = averageRating;
    }
    
    public long getMovieId() {
        return movieId;
    }
    
    public double getAverageRating() {
        return averageRating;
    }
    
    @Override
    public String toString() {
        return "RatingAverage{" +
                "movieId=" + movieId +
                ", averageRating=" + averageRating +
                '}';
    }
}