package com.YathzoBalo.repository;

import com.YathzoBalo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Leaderboard queries
    List<User> findTop10ByOrderByHighestScoreDesc();

    List<User> findTop10ByOrderByGamesWonDesc();

    @Query("SELECT u FROM User u ORDER BY (CAST(u.totalScore AS double) / CASE WHEN u.gamesPlayed = 0 THEN 1 ELSE u.gamesPlayed END) DESC")
    List<User> findTop10ByAverageScoreDesc();

    List<User> findTop10ByOrderByYahtzeesRolledDesc();

    // Statistics queries
    @Query("SELECT COUNT(u) FROM User u WHERE u.gamesPlayed > 0")
    Long countActiveUsers();

    @Query("SELECT SUM(u.gamesPlayed) FROM User u")
    Long getTotalGamesPlayed();

    @Query("SELECT AVG(u.highestScore) FROM User u WHERE u.gamesPlayed > 0")
    Double getAverageHighScore();
}
