package com.movietracker.api.repository;

import com.movietracker.api.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, String> {
    
    Optional<Movie> findByTitleIgnoreCase(String title);
    Optional<Movie> findByTmdbId(Integer tmdbId);
    Optional<Movie> findByImdbId(String imdbId);
    
    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(m.director) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Movie> searchByTitleOrDirector(@Param("query") String query);
    
    List<Movie> findByGenreIgnoreCase(String genre);
    List<Movie> findByReleaseYear(Integer year);
}
