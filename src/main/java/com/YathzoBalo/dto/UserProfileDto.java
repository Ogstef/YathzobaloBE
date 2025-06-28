package com.YathzoBalo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private Integer gamesPlayed;
    private Integer gamesWon;
    private Integer highestScore;
    private Long totalScore;
    private Integer yahtzeesRolled;
    private Double averageScore;
    private Double winRate;
    private LocalDateTime createdAt;
}
