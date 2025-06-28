package com.YathzoBalo.service;

import com.YathzoBalo.dto.*;
import com.YathzoBalo.entity.User;
import com.YathzoBalo.repository.UserRepository;
import com.YathzoBalo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public JwtResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName() != null ? request.getDisplayName() : request.getUsername());
        user.setIsEnabled(true);
        user.setIsAccountNonExpired(true);
        user.setIsAccountNonLocked(true);
        user.setIsCredentialsNonExpired(true);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser);

        return new JwtResponse(
                token,
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getDisplayName(),
                savedUser.getId()
        );
    }

    public JwtResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getUsername());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();
            log.info("User authenticated successfully: {}", user.getUsername());

            // Generate JWT token
            String token = jwtUtil.generateToken(user);

            return new JwtResponse(
                    token,
                    user.getUsername(),
                    user.getEmail(),
                    user.getDisplayName(),
                    user.getId()
            );

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {}", request.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    public JwtResponse refreshToken(String token) {
        log.debug("Refreshing token");

        try {
            String username = jwtUtil.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (jwtUtil.canTokenBeRefreshed(token)) {
                String refreshedToken = jwtUtil.refreshToken(token);

                return new JwtResponse(
                        refreshedToken,
                        user.getUsername(),
                        user.getEmail(),
                        user.getDisplayName(),
                        user.getId()
                );
            } else {
                throw new RuntimeException("Token cannot be refreshed");
            }

        } catch (Exception e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Invalid token for refresh");
        }
    }

    public UserProfileDto getCurrentUserProfile(String username) {
        log.debug("Getting profile for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToUserProfileDto(user);
    }

    public UserProfileDto updateProfile(String username, UpdateProfileRequestDto request) {
        log.info("Updating profile for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email is already in use!");
            }
            user.setEmail(request.getEmail());
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", username);

        return mapToUserProfileDto(updatedUser);
    }

    public void changePassword(String username, ChangePasswordRequestDto request) {
        log.info("Changing password for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", username);
    }

    private UserProfileDto mapToUserProfileDto(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getGamesPlayed(),
                user.getGamesWon(),
                user.getHighestScore(),
                user.getTotalScore(),
                user.getYahtzeesRolled(),
                user.getAverageScore(),
                user.getWinRate(),
                user.getCreatedAt()
        );
    }
}
