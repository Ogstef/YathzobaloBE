package com.YathzoBalo.util;

import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class YathzeeScoreCalculator {

    public static int calculateScore(List<Integer> dice, String category) {
        switch (category.toLowerCase()) {
            case "ones": return sumOfNumber(dice, 1);
            case "twos": return sumOfNumber(dice, 2);
            case "threes": return sumOfNumber(dice, 3);
            case "fours": return sumOfNumber(dice, 4);
            case "fives": return sumOfNumber(dice, 5);
            case "sixes": return sumOfNumber(dice, 6);
            case "threeofkind": return threeOfAKind(dice);
            case "fourofkind": return fourOfAKind(dice);
            case "fullhouse": return fullHouse(dice);
            case "smallstraight": return smallStraight(dice);
            case "largestraight": return largeStraight(dice);
            case "yahtzee": return yahtzee(dice);
            case "chance": return chance(dice);
            default: return 0;
        }
    }

    private static int sumOfNumber(List<Integer> dice, int number) {
        return dice.stream()
                .filter(die -> die == number)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private static int threeOfAKind(List<Integer> dice) {
        Map<Integer, Integer> counts = countDice(dice);
        for (int count : counts.values()) {
            if (count >= 3) {
                return dice.stream().mapToInt(Integer::intValue).sum();
            }
        }
        return 0;
    }

    private static int fourOfAKind(List<Integer> dice) {
        Map<Integer, Integer> counts = countDice(dice);
        for (int count : counts.values()) {
            if (count >= 4) {
                return dice.stream().mapToInt(Integer::intValue).sum();
            }
        }
        return 0;
    }

    private static int fullHouse(List<Integer> dice) {
        Map<Integer, Integer> counts = countDice(dice);
        boolean hasThree = false, hasTwo = false;

        for (int count : counts.values()) {
            if (count == 3) hasThree = true;
            if (count == 2) hasTwo = true;
        }

        return (hasThree && hasTwo) ? 25 : 0;
    }

    private static int smallStraight(List<Integer> dice) {
        Set<Integer> uniqueDice = new HashSet<>(dice);

        // Check for sequences of 4: 1-2-3-4, 2-3-4-5, 3-4-5-6
        if (uniqueDice.containsAll(Arrays.asList(1, 2, 3, 4)) ||
                uniqueDice.containsAll(Arrays.asList(2, 3, 4, 5)) ||
                uniqueDice.containsAll(Arrays.asList(3, 4, 5, 6))) {
            return 30;
        }

        return 0;
    }

    private static int largeStraight(List<Integer> dice) {
        Set<Integer> uniqueDice = new HashSet<>(dice);

        // Check for sequences of 5: 1-2-3-4-5 or 2-3-4-5-6
        if (uniqueDice.containsAll(Arrays.asList(1, 2, 3, 4, 5)) ||
                uniqueDice.containsAll(Arrays.asList(2, 3, 4, 5, 6))) {
            return 40;
        }

        return 0;
    }

    private static int yahtzee(List<Integer> dice) {
        Map<Integer, Integer> counts = countDice(dice);
        for (int count : counts.values()) {
            if (count == 5) {
                return 50;
            }
        }
        return 0;
    }

    private static int chance(List<Integer> dice) {
        return dice.stream().mapToInt(Integer::intValue).sum();
    }

    private static Map<Integer, Integer> countDice(List<Integer> dice) {
        return dice.stream()
                .collect(Collectors.groupingBy(
                        die -> die,
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));
    }

    public static int calculateUpperBonus(int upperTotal) {
        return upperTotal >= 63 ? 35 : 0;
    }
}
