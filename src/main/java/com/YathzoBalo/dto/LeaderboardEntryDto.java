package com.YathzoBalo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryDto  {
    private Long userId;
    private String username;
    private String displayName;
    private Integer value; // Score, games won, etc.
    private Integer rank;
    private String category; // "highest_score", "games_won", etc.
}
