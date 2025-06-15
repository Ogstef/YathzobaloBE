package com.YathzoBalo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_name", nullable = false)
    private String playerName;

    @Column(name = "current_round")
    private Integer currentRound = 1;

    @Column(name = "rolls_left")
    private Integer rollsLeft = 3;

    @Column(name = "dice_values")
    private String diceValues; // JSON string: "[1,2,3,4,5]"

    @Column(name = "selected_dice")
    private String selectedDice; // JSON string: "[0,2,4]"

    @Column(name = "game_complete")
    private Boolean gameComplete = false;

    @Column(name = "total_score")
    private Integer totalScore = 0;

    // Score sheet columns
    @Column(name = "ones")
    private Integer ones;

    @Column(name = "twos")
    private Integer twos;

    @Column(name = "threes")
    private Integer threes;

    @Column(name = "fours")
    private Integer fours;

    @Column(name = "fives")
    private Integer fives;

    @Column(name = "sixes")
    private Integer sixes;

    @Column(name = "upper_bonus")
    private Integer upperBonus = 0;

    @Column(name = "upper_total")
    private Integer upperTotal = 0;

    @Column(name = "three_of_kind")
    private Integer threeOfKind;

    @Column(name = "four_of_kind")
    private Integer fourOfKind;

    @Column(name = "full_house")
    private Integer fullHouse;

    @Column(name = "small_straight")
    private Integer smallStraight;

    @Column(name = "large_straight")
    private Integer largeStraight;

    @Column(name = "yahtzee")
    private Integer yahtzee;

    @Column(name = "chance")
    private Integer chance;

    @Column(name = "lower_total")
    private Integer lowerTotal = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}