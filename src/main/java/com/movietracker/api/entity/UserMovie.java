package com.movietracker.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "user_movies")
public class UserMovie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;
    
    // Ticket information
    private String theater;
    private String seatAssignment;
    
    @Column(nullable = false)
    private LocalDate dateWatched;
    
    private LocalTime showtime;
    private BigDecimal ticketPrice;
    
    // User experience
    @Min(1) @Max(10)
    private Integer personalRating;
    
    @Column(length = 1000)
    private String notes;
    
    // Image storage
    private String ticketImageUrl;
    private String ticketImageS3Key;
    
    // AI extraction metadata
    private BigDecimal aiConfidenceScore; // 0.00 to 1.00
    
    @Column(columnDefinition = "text")
    private String extractionMetadata; // JSON string of raw AI response
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Constructors
    public UserMovie() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public UserMovie(User user, Movie movie, LocalDate dateWatched) {
        this();
        this.user = user;
        this.movie = movie;
        this.dateWatched = dateWatched;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }
    public void setDateWatched(LocalDate dateWatched) { this.dateWatched = dateWatched; }
    
    public LocalTime getShowtime() { return showtime; }
    public void setShowtime(LocalTime showtime) { this.showtime = showtime; }
    
    public BigDecimal getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(BigDecimal ticketPrice) { this.ticketPrice = ticketPrice; }
    
    public Integer getPersonalRating() { return personalRating; }
    public void setPersonalRating(Integer personalRating) { this.personalRating = personalRating; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getTicketImageUrl() { return ticketImageUrl; }
    public void setTicketImageUrl(String ticketImageUrl) { this.ticketImageUrl = ticketImageUrl; }
    
    public String getTicketImageS3Key() { return ticketImageS3Key; }
    public void setTicketImageS3Key(String ticketImageS3Key) { this.ticketImageS3Key = ticketImageS3Key; }
    
    public BigDecimal getAiConfidenceScore() { return aiConfidenceScore; }
    public void setAiConfidenceScore(BigDecimal aiConfidenceScore) { this.aiConfidenceScore = aiConfidenceScore; }
    
    public String getExtractionMetadata() { return extractionMetadata; }
    public void setExtractionMetadata(String extractionMetadata) { this.extractionMetadata = extractionMetadata; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
