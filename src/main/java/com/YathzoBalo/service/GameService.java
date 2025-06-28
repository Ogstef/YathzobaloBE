package com.YathzoBalo.service;

import com.YathzoBalo.entity.Game;
import com.YathzoBalo.entity.User;
import com.YathzoBalo.repository.GameRepository;
import com.YathzoBalo.repository.UserRepository;
import com.YathzoBalo.dto.GameStateDto;
import com.YathzoBalo.util.YathzeeScoreCalculator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GameService {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    // Create new game for authenticated user
    public GameStateDto createNewGameForUser(User user) {
        Game game = new Game();
        game.setUser(user);
        game.setPlayerName(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
        game.setCurrentRound(1);
        game.setRollsLeft(3);
        game.setGameComplete(false);
        game.setTotalScore(0);

        // Initialize with random dice
        List<Integer> initialDice = generateRandomDice();
        game.setDiceValues(serializeList(initialDice));
        game.setSelectedDice("[]");

        Game savedGame = gameRepository.save(game);
        log.info("Created new game with ID: {} for user: {}", savedGame.getId(), user.getUsername());

        return convertToDto(savedGame);
    }

    // Roll dice with user authentication
    public GameStateDto rollDiceForUser(Long gameId, List<Integer> selectedDiceIndices, User user) {
        Game game = findGameByIdAndUser(gameId, user);

        if (game.getRollsLeft() <= 0) {
            throw new RuntimeException("No rolls left! Please select a score category.");
        }

        if (game.getGameComplete()) {
            throw new RuntimeException("Game is already complete!");
        }

        List<Integer> currentDice = deserializeList(game.getDiceValues());
        List<Integer> selectedIndices = selectedDiceIndices != null ? selectedDiceIndices : List.of();

        List<Integer> newDice = IntStream.range(0, 5)
                .mapToObj(i -> selectedIndices.contains(i) ? currentDice.get(i) : random.nextInt(6) + 1)
                .toList();

        game.setDiceValues(serializeList(newDice));
        game.setSelectedDice(serializeList(selectedIndices));
        game.setRollsLeft(game.getRollsLeft() - 1);

        Game savedGame = gameRepository.save(game);
        log.info("Rolled dice for game {} by user {}: {} (rolls left: {})",
                gameId, user.getUsername(), newDice, savedGame.getRollsLeft());

        return convertToDto(savedGame);
    }

    // Select score with user authentication
    public GameStateDto selectScoreForUser(Long gameId, String category, User user) {
        Game game = findGameByIdAndUser(gameId, user);

        if (game.getGameComplete()) {
            throw new RuntimeException("Game is already complete!");
        }

        if (isCategoryUsed(game, category)) {
            throw new RuntimeException("Score category '" + category + "' has already been used!");
        }

        List<Integer> dice = deserializeList(game.getDiceValues());
        int score = YathzeeScoreCalculator.calculateScore(dice, category);

        setScoreForCategory(game, category, score);
        updateTotals(game);

        // Update user statistics
        updateUserStatistics(user, game, category, score);

        game.setCurrentRound(game.getCurrentRound() + 1);
        game.setRollsLeft(3);
        game.setSelectedDice("[]");

        if (game.getCurrentRound() > 13) {
            game.setGameComplete(true);
            finalizeGame(game, user);
            log.info("Game {} completed by user {}! Final score: {}",
                    gameId, user.getUsername(), game.getTotalScore());
        }

        Game savedGame = gameRepository.save(game);
        log.info("Scored {} points in category '{}' for game {} by user {}",
                score, category, gameId, user.getUsername());

        return convertToDto(savedGame);
    }

    // Get game state with user authentication
    public GameStateDto getGameStateForUser(Long gameId, User user) {
        Game game = findGameByIdAndUser(gameId, user);
        return convertToDto(game);
    }

    // Get all games for user
    public List<GameStateDto> getUserGames(User user) {
        return gameRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    // Get active games for user
    public List<GameStateDto> getUserActiveGames(User user) {
        return gameRepository.findByUserAndGameCompleteOrderByUpdatedAtDesc(user, false)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    // Get completed games for user
    public List<GameStateDto> getUserCompletedGames(User user) {
        return gameRepository.findByUserAndGameCompleteOrderByTotalScoreDesc(user, true)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    // Delete game with user authentication
    public void deleteGameForUser(Long gameId, User user) {
        Game game = findGameByIdAndUser(gameId, user);

        if (game.getGameComplete()) {
            throw new RuntimeException("Cannot delete completed games!");
        }

        gameRepository.delete(game);
        log.info("Deleted game {} by user {}", gameId, user.getUsername());
    }

    // Public methods (no authentication required)
    public List<GameStateDto> getLeaderboard() {
        return gameRepository.findTop10ByOrderByTotalScoreDesc()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<GameStateDto> getHighestScoresLeaderboard() {
        return gameRepository.findTop20ByGameCompleteOrderByTotalScoreDesc(true)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    public Map<String, Object> getGlobalStatistics() {
        Map<String, Object> stats = new HashMap<>();

        Long totalGames = gameRepository.countByGameComplete(true);
        Long activeGames = gameRepository.countByGameComplete(false);
        Long totalUsers = userRepository.count();

        stats.put("totalCompletedGames", totalGames);
        stats.put("activeGames", activeGames);
        stats.put("totalUsers", totalUsers);

        if (totalGames > 0) {
            Double avgScore = gameRepository.findAverageScoreOfCompletedGames();
            Integer highestScore = gameRepository.findHighestScore();
            stats.put("averageScore", avgScore != null ? avgScore : 0.0);
            stats.put("highestScore", highestScore != null ? highestScore : 0);
        }

        return stats;
    }

    // Helper method to find game by ID and verify ownership
    private Game findGameByIdAndUser(Long gameId, User user) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with ID: " + gameId));

        if (!game.belongsToUser(user)) {
            throw new AccessDeniedException("You don't have permission to access this game");
        }

        return game;
    }

    // Update user statistics when game actions occur
    private void updateUserStatistics(User user, Game game, String category, int score) {
        if ("yahtzee".equals(category) && score == 50) {
            user.incrementYahtzees();
            userRepository.save(user);
        }
    }

    // Finalize game and update user stats
    private void finalizeGame(Game game, User user) {
        user.incrementGamesPlayed();
        user.updateHighestScore(game.getTotalScore());
        user.addToTotalScore(game.getTotalScore());

        userRepository.save(user);
    }

    // ... (keep all the existing private helper methods)
    private boolean isCategoryUsed(Game game, String category) {
        return switch (category.toLowerCase()) {
            case "ones" -> game.getOnes() != null;
            case "twos" -> game.getTwos() != null;
            case "threes" -> game.getThrees() != null;
            case "fours" -> game.getFours() != null;
            case "fives" -> game.getFives() != null;
            case "sixes" -> game.getSixes() != null;
            case "threeofkind" -> game.getThreeofkind() != null;
            case "fourofkind" -> game.getFourofkind() != null;
            case "fullhouse" -> game.getFullhouse() != null;
            case "smallstraight" -> game.getSmallstraight() != null;
            case "largestraight" -> game.getLargestraight() != null;
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
            case "threeofkind" -> game.setThreeofkind(score);
            case "fourofkind" -> game.setFourofkind(score);
            case "fullhouse" -> game.setFullhouse(score);
            case "smallstraight" -> game.setSmallstraight(score);
            case "largestraight" -> game.setLargestraight(score);
            case "yahtzee" -> game.setYahtzee(score);
            case "chance" -> game.setChance(score);
            default -> throw new RuntimeException("Invalid score category: " + category);
        }
    }

    private void updateTotals(Game game) {
        int upperSum = safeSum(game.getOnes(), game.getTwos(), game.getThrees(),
                game.getFours(), game.getFives(), game.getSixes());

        int bonus = upperSum >= 63 ? 35 : 0;
        game.setUpperBonus(bonus);
        game.setUpperTotal(upperSum + bonus);

        int lowerSum = safeSum(game.getThreeofkind(), game.getFourofkind(), game.getFullhouse(),
                game.getSmallstraight(), game.getLargestraight(), game.getYahtzee(), game.getChance());
        game.setLowerTotal(lowerSum);

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
                game.getThreeofkind(), game.getFourofkind(), game.getFullhouse(),
                game.getSmallstraight(), game.getLargestraight(), game.getYahtzee(), game.getChance(),
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