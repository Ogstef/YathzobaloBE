package com.YathzoBalo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;


public class GameRequestDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewGameRequest {
        @NotBlank(message = "Player name is required")
        private String playerName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RollDiceRequest {
        @NotNull(message = "Game ID is required")
        private Long gameId;
        private List<Integer> selectedDice; // Dice to keep (indices)
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreSelectionRequest {
        @NotNull(message = "Game ID is required")
        private Long gameId;
        @NotBlank(message = "Score category is required")
        private String category; // "ones", "twos", "yahtzee", etc.
    }
}
