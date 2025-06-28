package com.YathzoBalo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "is_enabled")
    private Boolean isEnabled = true;

    @Column(name = "is_account_non_expired")
    private Boolean isAccountNonExpired = true;

    @Column(name = "is_account_non_locked")
    private Boolean isAccountNonLocked = true;

    @Column(name = "is_credentials_non_expired")
    private Boolean isCredentialsNonExpired = true;

    // Game Statistics
    @Column(name = "games_played")
    private Integer gamesPlayed = 0;

    @Column(name = "games_won")
    private Integer gamesWon = 0;

    @Column(name = "highest_score")
    private Integer highestScore = 0;

    @Column(name = "total_score")
    private Long totalScore = 0L;

    @Column(name = "yahtzees_rolled")
    private Integer yahtzeesRolled = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isAccountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isCredentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    // Helper methods
    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }

    public void incrementGamesWon() {
        this.gamesWon++;
    }

    public void updateHighestScore(Integer score) {
        if (score > this.highestScore) {
            this.highestScore = score;
        }
    }

    public void addToTotalScore(Integer score) {
        this.totalScore += score;
    }

    public void incrementYahtzees() {
        this.yahtzeesRolled++;
    }

    public Double getAverageScore() {
        if (gamesPlayed == 0) return 0.0;
        return (double) totalScore / gamesPlayed;
    }

    public Double getWinRate() {
        if (gamesPlayed == 0) return 0.0;
        return (double) gamesWon / gamesPlayed * 100;
    }
}