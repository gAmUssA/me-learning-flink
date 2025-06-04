package io.confluent.developer.movies.domain;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Movie implements Serializable {

  private static final long serialVersionUID = 1L;

  public long movieId;
  public String title;
  public int releaseYear;
  public String country;
  public double rating;
  public ArrayList<String> genres;        // Use ArrayList directly
  public ArrayList<String> actors;        // Use ArrayList directly
  public ArrayList<String> directors;     // Use ArrayList directly
  public ArrayList<String> composers;     // Use ArrayList directly
  public ArrayList<String> screenwriters; // Use ArrayList directly
  public ArrayList<String> productionCompanies; // Use ArrayList directly
  public String cinematographer;

  public Movie() {
    initializeFields();
  }

  private void initializeFields() {
    this.movieId = 0;
    this.title = "";
    this.releaseYear = 0;
    this.country = "";
    this.rating = 0.0;
    this.genres = new ArrayList<>();
    this.actors = new ArrayList<>();
    this.directors = new ArrayList<>();
    this.composers = new ArrayList<>();
    this.screenwriters = new ArrayList<>();
    this.productionCompanies = new ArrayList<>();
    this.cinematographer = "";
  }

  @JsonCreator
  public Movie(
      @JsonProperty("movieId") long movieId,
      @JsonProperty("title") String title,
      @JsonProperty("releaseYear") int releaseYear,
      @JsonProperty("country") String country,
      @JsonProperty("rating") double rating,
      @JsonProperty("genres") Collection<String> genres,
      @JsonProperty("actors") Collection<String> actors,
      @JsonProperty("directors") Collection<String> directors,
      @JsonProperty("composers") Collection<String> composers,
      @JsonProperty("screenwriters") Collection<String> screenwriters,
      @JsonProperty("productionCompanies") Collection<String> productionCompanies,
      @JsonProperty("cinematographer") String cinematographer) {
    initializeFields(); // Initialize with empty collections first

    this.movieId = movieId;
    this.title = title != null ? title : "";
    this.releaseYear = releaseYear;
    this.country = country != null ? country : "";
    this.rating = rating;

    // Safely copy collections, handling null cases
    if (genres != null) {
      this.genres.addAll(genres);
    }
    if (actors != null) {
      this.actors.addAll(actors);
    }
    if (directors != null) {
      this.directors.addAll(directors);
    }
    if (composers != null) {
      this.composers.addAll(composers);
    }
    if (screenwriters != null) {
      this.screenwriters.addAll(screenwriters);
    }
    if (productionCompanies != null) {
      this.productionCompanies.addAll(productionCompanies);
    }

    this.cinematographer = cinematographer != null ? cinematographer : "";
  }

  // Getters return the internal ArrayList directly
  public ArrayList<String> getGenres() {
    return genres;
  }

  public ArrayList<String> getActors() {
    return actors;
  }

  public ArrayList<String> getDirectors() {
    return directors;
  }

  public ArrayList<String> getComposers() {
    return composers;
  }

  public ArrayList<String> getScreenwriters() {
    return screenwriters;
  }

  public ArrayList<String> getProductionCompanies() {
    return productionCompanies;
  }

  // Other getters
  public long getMovieId() {
    return movieId;
  }

  public String getTitle() {
    return title;
  }

  public int getReleaseYear() {
    return releaseYear;
  }

  public String getCountry() {
    return country;
  }

  public double getRating() {
    return rating;
  }

  public String getCinematographer() {
    return cinematographer;
  }

  // Setters that ensure ArrayList is preserved
  public void setGenres(Collection<String> genres) {
    this.genres.clear();
    if (genres != null) {
      this.genres.addAll(genres);
    }
  }

  public void setActors(Collection<String> actors) {
    this.actors.clear();
    if (actors != null) {
      this.actors.addAll(actors);
    }
  }

  public void setDirectors(Collection<String> directors) {
    this.directors.clear();
    if (directors != null) {
      this.directors.addAll(directors);
    }
  }

  public void setComposers(Collection<String> composers) {
    this.composers.clear();
    if (composers != null) {
      this.composers.addAll(composers);
    }
  }

  public void setScreenwriters(Collection<String> screenwriters) {
    this.screenwriters.clear();
    if (screenwriters != null) {
      this.screenwriters.addAll(screenwriters);
    }
  }

  public void setProductionCompanies(Collection<String> productionCompanies) {
    this.productionCompanies.clear();
    if (productionCompanies != null) {
      this.productionCompanies.addAll(productionCompanies);
    }
  }

  // Other setters
  public void setMovieId(long movieId) {
    this.movieId = movieId;
  }

  public void setTitle(String title) {
    this.title = title != null ? title : "";
  }

  public void setReleaseYear(int releaseYear) {
    this.releaseYear = releaseYear;
  }

  public void setCountry(String country) {
    this.country = country != null ? country : "";
  }

  public void setRating(double rating) {
    this.rating = rating;
  }

  public void setCinematographer(String cinematographer) {
    this.cinematographer = cinematographer != null ? cinematographer : "";
  }
}
