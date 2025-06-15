package com.YathzoBalo.service;

import com.YathzoBalo.dto.GameStateDto;
import com.YathzoBalo.entity.Game;
import com.YathzoBalo.repository.GameRepository;
import com.YathzoBalo.util.YathzeeScoreCalculator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GameService {

    private final GameRepository gameRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    public GameStateDto createNewGame(String playerName) {
        Game game = new Game();
        game.setPlayerName(playerName);
        game.setCurrentRound(1);
        game.setRollsLeft(3);
        game.setGameComplete(false);
        game.setTotalScore(0);

        // Initialize with random dice
        List<Integer> initialDice = generateRandomDice();
        game.setDiceValues(serializeList(initialDice));
        game.setSelectedDice("[]"); // No dice selected initially

        Game savedGame = gameRepository.save(game);
        log.info("Created new game with ID: {} for player: {}", savedGame.getId(), playerName);

        return convertToDto(savedGame);
    }

    public GameStateDto rollDice(Long gameId, List<Integer> selectedDiceIndices) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with ID: " + gameId));

        if (game.getRollsLeft() <= 0) {
            throw new RuntimeException("No rolls left! Please select a score category.");
        }

        if (game.getGameComplete()) {
            throw new RuntimeException("Game is already complete!");
        }

        List<Integer> currentDice = deserializeList(game.getDiceValues());
        List<Integer> selectedIndices = selectedDiceIndices != null ? selectedDiceIndices : List.of();

        // Roll dice that are NOT selected (keep selected ones)
        List<Integer> newDice = IntStream.range(0, 5)
                .mapToObj(i -> selectedIndices.contains(i) ? currentDice.get(i) : random.nextInt(6) + 1)
                .toList();

        game.setDiceValues(serializeList(newDice));
        game.setSelectedDice(serializeList(selectedIndices));
        game.setRollsLeft(game.getRollsLeft() - 1);

        Game savedGame = gameRepository.save(game);
        log.info("Rolled dice for game {}: {} (rolls left: {})", gameId, newDice, savedGame.getRollsLeft());
        log.error("Risky business");
        return convertToDto(savedGame);
    }

    public GameStateDto selectScore(Long gameId, String category) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with ID: " + gameId));

        if (game.getGameComplete()) {
            throw new RuntimeException("Game is already complete!");
        }

        // Check if category is already used
        if (isCategoryUsed(game, category)) {
            throw new RuntimeException("Score category '" + category + "' has already been used!");
        }

        List<Integer> dice = deserializeList(game.getDiceValues());
        int score = YathzeeScoreCalculator.calculateScore(dice, category);

        // Set the score in the appropriate field
        setScoreForCategory(game, category, score);

        // Update totals and bonuses
        updateTotals(game);

        // Move to next round
        game.setCurrentRound(game.getCurrentRound() + 1);
        game.setRollsLeft(3);
        game.setSelectedDice("[]");

        // Check if game is complete (13 rounds)
        if (game.getCurrentRound() > 13) {
            game.setGameComplete(true);
            log.info("Game {} completed! Final score: {}", gameId, game.getTotalScore());
        }

        Game savedGame = gameRepository.save(game);
        log.info("Scored {} points in category '{}' for game {}", score, category, gameId);

        return convertToDto(savedGame);
    }

    public GameStateDto getGameState(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with ID: " + gameId));
        return convertToDto(game);
    }

    public List<GameStateDto> getPlayerGames(String playerName) {
        return gameRepository.findByPlayerNameOrderByCreatedAtDesc(playerName)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<GameStateDto> getLeaderboard() {
        return gameRepository.findTop10ByOrderByTotalScoreDesc()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    private boolean isCategoryUsed(Game game, String category) {
        return switch (category.toLowerCase()) {
            case "ones" -> game.getOnes() != null;
            case "twos" -> game.getTwos() != null;
            case "threes" -> game.getThrees() != null;
            case "fours" -> game.getFours() != null;
            case "fives" -> game.getFives() != null;
            case "sixes" -> game.getSixes() != null;
            case "threeofkind" -> game.getThreeOfKind() != null;
            case "fourofkind" -> game.getFourOfKind() != null;
            case "fullhouse" -> game.getFullHouse() != null;
            case "smallstraight" -> game.getSmallStraight() != null;
            case "largestraight" -> game.getLargeStraight() != null;
            case "yahtzee" -> game.getYahtzee() != null;
            case "chance" -> game.getChance() != null;
            default -> throw new RuntimeException("Invalid score category: " + category);
        };
    }

    private void setScoreForCategory(Game game, String category, int score) {
        switch (category.toLowerCase()) {
            case "ones" -> game.setOnes(score);
            case "twos" -> game.setTwos(score);
            case "threes" -> game.setThrees(score);
            case "fours" -> game.setFours(score);
            case "fives" -> game.setFives(score);
            case "sixes" -> game.setSixes(score);
            case "threeofkind" -> game.setThreeOfKind(score);
            case "fourofkind" -> game.setFourOfKind(score);
            case "fullhouse" -> game.setFullHouse(score);
            case "smallstraight" -> game.setSmallStraight(score);
            case "largestraight" -> game.setLargeStraight(score);
            case "yahtzee" -> game.setYahtzee(score);
            case "chance" -> game.setChance(score);
            default -> throw new RuntimeException("Invalid score category: " + category);
        }
    }

    private void updateTotals(Game game) {
        // Calculate upper section total
        int upperSum = safeSum(game.getOnes(), game.getTwos(), game.getThrees(),
                game.getFours(), game.getFives(), game.getSixes());

        // Calculate bonus (35 points if upper section >= 63)
        int bonus = upperSum >= 63 ? 35 : 0;
        game.setUpperBonus(bonus);
        game.setUpperTotal(upperSum + bonus);

        // Calculate lower section total
        int lowerSum = safeSum(game.getThreeOfKind(), game.getFourOfKind(), game.getFullHouse(),
                game.getSmallStraight(), game.getLargeStraight(), game.getYahtzee(), game.getChance());
        game.setLowerTotal(lowerSum);

        // Calculate total score
        game.setTotalScore(game.getUpperTotal() + game.getLowerTotal());
    }

    private int safeSum(Integer... values) {
        return Arrays.stream(values)
                .filter(val -> val != null)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private List<Integer> generateRandomDice() {
        return IntStream.range(0, 5)
                .map(i -> random.nextInt(6) + 1)
                .boxed()
                .toList();
    }

    private GameStateDto convertToDto(Game game) {
        GameStateDto.ScoreSheetDto scoreSheet = new GameStateDto.ScoreSheetDto(
                game.getOnes(), game.getTwos(), game.getThrees(), game.getFours(), game.getFives(), game.getSixes(),
                game.getUpperBonus(), game.getUpperTotal(),
                game.getThreeOfKind(), game.getFourOfKind(), game.getFullHouse(),
                game.getSmallStraight(), game.getLargeStraight(), game.getYahtzee(), game.getChance(),
                game.getLowerTotal()
        );

        return new GameStateDto(
                game.getId(),
                game.getPlayerName(),
                game.getCurrentRound(),
                game.getRollsLeft(),
                deserializeList(game.getDiceValues()),
                deserializeList(game.getSelectedDice()),
                game.getGameComplete(),
                game.getTotalScore(),
                scoreSheet
        );
    }

    private String serializeList(List<Integer> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing list", e);
        }
    }

    private List<Integer> deserializeList(String json) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing list", e);
        }
    }

}
