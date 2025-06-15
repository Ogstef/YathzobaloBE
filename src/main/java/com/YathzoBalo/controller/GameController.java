package com.YathzoBalo.controller;


import com.YathzoBalo.dto.GameRequestDto;
import com.YathzoBalo.dto.GameStateDto;
import com.YathzoBalo.service.GameService;
import static com.YathzoBalo.util.YathzeeScoreCalculator.calculateScore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/games")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Allow React Native to call these endpoints
public class GameController {

    private final GameService gameService;

    @PostMapping("/new")
    public ResponseEntity<GameStateDto> createNewGame(@Valid @RequestBody GameRequestDto.NewGameRequest request) {
        log.info("Creating new game for player: {}", request.getPlayerName());
        GameStateDto gameState = gameService.createNewGame(request.getPlayerName());
        return ResponseEntity.ok(gameState);
    }

    @PostMapping("/roll")
    public ResponseEntity<GameStateDto> rollDice(@Valid @RequestBody GameRequestDto.RollDiceRequest request) {
        log.info("Rolling dice for game: {} with selected dice: {}", request.getGameId(), request.getSelectedDice());
        GameStateDto gameState = gameService.rollDice(request.getGameId(), request.getSelectedDice());
        return ResponseEntity.ok(gameState);
    }

    @PostMapping("/score")
    public ResponseEntity<GameStateDto> selectScore(@Valid @RequestBody GameRequestDto.ScoreSelectionRequest request) {
        log.info("Selecting score category '{}' for game: {}", request.getCategory(), request.getGameId());
        GameStateDto gameState = gameService.selectScore(request.getGameId(), request.getCategory());
        return ResponseEntity.ok(gameState);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateDto> getGameState(@PathVariable Long gameId) {
        log.info("Getting game state for game: {}", gameId);
        GameStateDto gameState = gameService.getGameState(gameId);
        return ResponseEntity.ok(gameState);
    }

    @GetMapping("/player/{playerName}")
    public ResponseEntity<List<GameStateDto>> getPlayerGames(@PathVariable String playerName) {
        log.info("Getting games for player: {}", playerName);
        List<GameStateDto> games = gameService.getPlayerGames(playerName);
        return ResponseEntity.ok(games);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<GameStateDto>> getLeaderboard() {
        log.info("Getting leaderboard");
        List<GameStateDto> leaderboard = gameService.getLeaderboard();
        return ResponseEntity.ok(leaderboard);
    }

    // Helper endpoint to check possible scores for current dice
// Helper endpoint to check possible scores for current dice

    @GetMapping("/{gameId}/possible-scores")
    public ResponseEntity<Map<String, Integer>> getPossibleScores(@PathVariable Long gameId) {
        GameStateDto gameState = gameService.getGameState(gameId);
        List<Integer> dice = gameState.getDice();

        Map<String, Integer> possibleScores = new HashMap<>();
        possibleScores.put("ones", calculateScore(dice, "ones"));
        possibleScores.put("twos", calculateScore(dice, "twos"));
        possibleScores.put("threes", calculateScore(dice, "threes"));
        possibleScores.put("fours", calculateScore(dice, "fours"));
        possibleScores.put("fives", calculateScore(dice, "fives"));
        possibleScores.put("sixes", calculateScore(dice, "sixes"));
        possibleScores.put("threeOfKind", calculateScore(dice, "threeofkind"));
        possibleScores.put("fourOfKind", calculateScore(dice, "fourofkind"));
        possibleScores.put("fullHouse", calculateScore(dice, "fullhouse"));
        possibleScores.put("smallStraight", calculateScore(dice, "smallstraight"));
        possibleScores.put("largeStraight", calculateScore(dice, "largestraight"));
        possibleScores.put("yahtzee", calculateScore(dice, "yahtzee"));
        possibleScores.put("chance", calculateScore(dice, "chance"));

        return ResponseEntity.ok(possibleScores);
    }

}
