package com.movietracker.api.repository;

import com.movietracker.api.entity.UserMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserMovieRepository extends JpaRepository<UserMovie, String> {
    
    List<UserMovie> findByUserIdOrderByDateWatchedDesc(String userId);
    List<UserMovie> findByUserId(String userId);
    
    @Query("SELECT um FROM UserMovie um WHERE um.user.id = :userId " +
           "AND um.dateWatched BETWEEN :startDate AND :endDate " +
           "ORDER BY um.dateWatched DESC")
    List<UserMovie> findByUserIdAndDateRange(
        @Param("userId") String userId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT COUNT(um) FROM UserMovie um WHERE um.user.id = :userId")
    Long countByUserId(@Param("userId") String userId);
    
    @Query("SELECT um FROM UserMovie um WHERE um.user.id = :userId " +
           "AND um.movie.genre = :genre ORDER BY um.dateWatched DESC")
    List<UserMovie> findByUserIdAndGenre(@Param("userId") String userId, @Param("genre") String genre);
}
