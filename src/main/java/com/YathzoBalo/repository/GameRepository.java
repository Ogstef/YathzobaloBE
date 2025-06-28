package com.YathzoBalo.repository;

import com.YathzoBalo.entity.Game;
import com.YathzoBalo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    // User-specific queries
    List<Game> findByUserOrderByCreatedAtDesc(User user);

    List<Game> findByUserAndGameCompleteOrderByUpdatedAtDesc(User user, Boolean gameComplete);

    List<Game> findByUserAndGameCompleteOrderByTotalScoreDesc(User user, Boolean gameComplete);

    Optional<Game> findByIdAndUser(Long id, User user);

    Long countByUserAndGameComplete(User user, Boolean gameComplete);

    // Legacy queries (for backward compatibility)
    List<Game> findByPlayerNameOrderByCreatedAtDesc(String playerName);

    Optional<Game> findByIdAndPlayerName(Long id, String playerName);

    // Leaderboard and statistics queries
    List<Game> findTop10ByOrderByTotalScoreDesc();

    List<Game> findTop20ByGameCompleteOrderByTotalScoreDesc(Boolean gameComplete);

    List<Game> findByGameCompleteOrderByTotalScoreDesc(Boolean gameComplete);

    Long countByGameComplete(Boolean gameComplete);

    @Query("SELECT AVG(g.totalScore) FROM Game g WHERE g.gameComplete = true")
    Double findAverageScoreOfCompletedGames();

    @Query("SELECT MAX(g.totalScore) FROM Game g WHERE g.gameComplete = true")
    Integer findHighestScore();

    @Query("SELECT COUNT(g) FROM Game g WHERE g.gameComplete = true AND g.yahtzee = 50")
    Long countYahtzeeGames();

    @Query("SELECT g FROM Game g WHERE g.gameComplete = true ORDER BY g.totalScore DESC LIMIT 10")
    List<Game> findTop10CompletedGamesByScore();

    // User statistics
    @Query("SELECT COUNT(g) FROM Game g WHERE g.user = :user AND g.gameComplete = true")
    Long countCompletedGamesByUser(User user);

    @Query("SELECT MAX(g.totalScore) FROM Game g WHERE g.user = :user AND g.gameComplete = true")
    Integer findHighestScoreByUser(User user);

    @Query("SELECT AVG(g.totalScore) FROM Game g WHERE g.user = :user AND g.gameComplete = true")
    Double findAverageScoreByUser(User user);
}