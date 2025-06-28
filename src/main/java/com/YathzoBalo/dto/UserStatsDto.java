package com.YathzoBalo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
    private Integer gamesPlayed;
    private Integer gamesWon;
    private Integer highestScore;
    private Long totalScore;
    private Integer yahtzeesRolled;
    private Double averageScore;
    private Double winRate;
    private Integer rank; // Global ranking
}
