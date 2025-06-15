package com.YathzoBalo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameStateDto {
    private Long gameId;
    private String playerName;
    private Integer currentRound;
    private Integer rollsLeft;
    private List<Integer> dice;
    private List<Integer> selectedDice;
    private Boolean gameComplete;
    private Integer totalScore;
    private ScoreSheetDto scoreSheet;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreSheetDto {
        private Integer ones;
        private Integer twos;
        private Integer threes;
        private Integer fours;
        private Integer fives;
        private Integer sixes;
        private Integer upperBonus;
        private Integer upperTotal;
        private Integer threeOfKind;
        private Integer fourOfKind;
        private Integer fullHouse;
        private Integer smallStraight;
        private Integer largeStraight;
        private Integer yahtzee;
        private Integer chance;
        private Integer lowerTotal;
    }
}