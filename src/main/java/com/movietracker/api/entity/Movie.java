package com.movietracker.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "movies")
public class Movie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    @NotBlank
    private String title;
    
    private Integer releaseYear;
    private String genre;
    private String rating; // PG, PG-13, R, etc.
    private Integer runtimeMinutes;
    private String director;
    private String posterUrl;
    private String plotSummary;
    
    // External IDs
    private Integer tmdbId; // The Movie Database ID
    private String imdbId;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Constructors
    public Movie() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Movie(String title) {
        this();
        this.title = title;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
    
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    
    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }
    
    public Integer getRuntimeMinutes() { return runtimeMinutes; }
    public void setRuntimeMinutes(Integer runtimeMinutes) { this.runtimeMinutes = runtimeMinutes; }
    
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    
    public String getPlotSummary() { return plotSummary; }
    public void setPlotSummary(String plotSummary) { this.plotSummary = plotSummary; }
    
    public Integer getTmdbId() { return tmdbId; }
    public void setTmdbId(Integer tmdbId) { this.tmdbId = tmdbId; }
    
    public String getImdbId() { return imdbId; }
    public void setImdbId(String imdbId) { this.imdbId = imdbId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
