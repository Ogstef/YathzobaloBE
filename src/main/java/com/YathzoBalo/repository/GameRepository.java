package com.YathzoBalo.repository;

import com.YathzoBalo.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByPlayerNameOrderByCreatedAtDesc(String playerName);

    List<Game> findByGameCompleteOrderByTotalScoreDesc(Boolean gameComplete);

    @Query("SELECT g FROM Game g WHERE g.gameComplete = true ORDER BY g.totalScore DESC LIMIT 10")
    List<Game> findTop10ByOrderByTotalScoreDesc();

    Optional<Game> findByIdAndPlayerName(Long id, String playerName);
}