package com.YathzoBalo.controller;

import com.YathzoBalo.dto.GameRequestDto;
import com.YathzoBalo.dto.GameStateDto;
import com.YathzoBalo.entity.User;
import com.YathzoBalo.service.GameService;
import static com.YathzoBalo.util.YathzeeScoreCalculator.calculateScore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game") // Changed from /api/public/games to /api/game (protected)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GameController {

    private final GameService gameService;

    @PostMapping("/new")
    public ResponseEntity<GameStateDto> createNewGame(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("Creating new game for user: {}", user.getUsername());

        GameStateDto gameState = gameService.createNewGameForUser(user);
        return ResponseEntity.ok(gameState);
    }

    @PostMapping("/roll")
    public ResponseEntity<GameStateDto> rollDice(
            @Valid @RequestBody GameRequestDto.RollDiceRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("Rolling dice for game: {} by user: {}", request.getGameId(), user.getUsername());

        GameStateDto gameState = gameService.rollDiceForUser(request.getGameId(), request.getSelectedDice(), user);
        return ResponseEntity.ok(gameState);
    }

    @PostMapping("/score")
    public ResponseEntity<GameStateDto> selectScore(
            @Valid @RequestBody GameRequestDto.ScoreSelectionRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("Selecting score category '{}' for game: {} by user: {}",
                request.getCategory(), request.getGameId(), user.getUsername());

        GameStateDto gameState = gameService.selectScoreForUser(request.getGameId(), request.getCategory(), user);
        return ResponseEntity.ok(gameState);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateDto> getGameState(
            @PathVariable Long gameId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("Getting game state for game: {} by user: {}", gameId, user.getUsername());

        GameStateDto gameState = gameService.getGameStateForUser(gameId, user);
        return ResponseEntity.ok(gameState);
    }

    @GetMapping("/my-games")
    public ResponseEntity<List<GameStateDto>> getMyGames(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("Getting games for user: {}", user.getUsername());

        List<GameStateDto> games = gameService.getUserGames(user);
        return ResponseEntity.ok(games);
    }

    @GetMapping("/my-games/active")
    public ResponseEntity<List<GameStateDto>> getMyActiveGames(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("Getting active games for user: {}", user.getUsername());

        List<GameStateDto> games = gameService.getUserActiveGames(user);
        return ResponseEntity.ok(games);
    }

    @GetMapping("/my-games/completed")
    public ResponseEntity<List<GameStateDto>> getMyCompletedGames(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("Getting completed games for user: {}", user.getUsername());

        List<GameStateDto> games = gameService.getUserCompletedGames(user);
        return ResponseEntity.ok(games);
    }

    @GetMapping("/{gameId}/possible-scores")
    public ResponseEntity<Map<String, Integer>> getPossibleScores(
            @PathVariable Long gameId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        GameStateDto gameState = gameService.getGameStateForUser(gameId, user);
        List<Integer> dice = gameState.getDice();

        Map<String, Integer> possibleScores = new HashMap<>();
        possibleScores.put("ones", calculateScore(dice, "ones"));
        possibleScores.put("twos", calculateScore(dice, "twos"));
        possibleScores.put("threes", calculateScore(dice, "threes"));
        possibleScores.put("fours", calculateScore(dice, "fours"));
        possibleScores.put("fives", calculateScore(dice, "fives"));
        possibleScores.put("sixes", calculateScore(dice, "sixes"));
        possibleScores.put("threeofkind", calculateScore(dice, "threeofkind"));
        possibleScores.put("fourofkind", calculateScore(dice, "fourofkind"));
        possibleScores.put("fullhouse", calculateScore(dice, "fullhouse"));
        possibleScores.put("smallstraight", calculateScore(dice, "smallstraight"));
        possibleScores.put("largestraight", calculateScore(dice, "largestraight"));
        possibleScores.put("yahtzee", calculateScore(dice, "yahtzee"));
        possibleScores.put("chance", calculateScore(dice, "chance"));

        return ResponseEntity.ok(possibleScores);
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<?> deleteGame(
            @PathVariable Long gameId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("Deleting game: {} by user: {}", gameId, user.getUsername());

        gameService.deleteGameForUser(gameId, user);
        return ResponseEntity.ok(Map.of("message", "Game deleted successfully"));
    }
}

// Keep a separate controller for public endpoints (leaderboards, etc.)
@RestController
@RequestMapping("/api/public/games")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
class PublicGameController {

    private final GameService gameService;

    @GetMapping("/leaderboard")
    public ResponseEntity<List<GameStateDto>> getLeaderboard() {
        log.info("Getting global leaderboard");
        List<GameStateDto> leaderboard = gameService.getLeaderboard();
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/leaderboard/highest-scores")
    public ResponseEntity<List<GameStateDto>> getHighestScores() {
        log.info("Getting highest scores leaderboard");
        List<GameStateDto> leaderboard = gameService.getHighestScoresLeaderboard();
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/stats/global")
    public ResponseEntity<Map<String, Object>> getGlobalStats() {
        log.info("Getting global game statistics");
        Map<String, Object> stats = gameService.getGlobalStatistics();
        return ResponseEntity.ok(stats);
    }
}