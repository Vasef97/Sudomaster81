package com.ltp.sudomaster.service;

import com.ltp.sudomaster.dto.AuthResponse;
import com.ltp.sudomaster.dto.LoginRequest;
import com.ltp.sudomaster.dto.RegisterRequest;
import com.ltp.sudomaster.entity.User;
import com.ltp.sudomaster.entity.SudokuGameSession;
import com.ltp.sudomaster.repository.UserRepository;
import com.ltp.sudomaster.repository.SudokuGameSessionRepository;
import com.ltp.sudomaster.repository.SudokuPuzzleRepository;
import com.ltp.sudomaster.repository.GameScoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserAuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SudokuGameSessionRepository sudokuGameSessionRepository;

    @Autowired
    private SudokuPuzzleRepository sudokuPuzzleRepository;

    @Autowired
    private GameScoreRepository gameScoreRepository;

    private static final long TOKEN_EXPIRY_MS = 72 * 60 * 60 * 1000L;

    private String generateBearerToken(String userId) {
        String tokenData = userId + ":" + System.currentTimeMillis();
        return Base64.getEncoder().encodeToString(tokenData.getBytes());
    }

    @Transactional
    @SuppressWarnings("null")
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered: {}", savedUser.getUsername());

        String token = generateBearerToken(savedUser.getId());

        return AuthResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .accessToken(token)
                .tokenExpiresIn(TOKEN_EXPIRY_MS)
                .tokenType("Bearer")
                .message("Registration successful")
                .preferencesJson(savedUser.getPreferencesJson())
                .build();
    }

    @Transactional
    @SuppressWarnings("null")
    public AuthResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmailOrUsername(request.getEmailOrUsername(), request.getEmailOrUsername());

        User user = userOpt.orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.getIsActive()) {
            throw new IllegalArgumentException("User account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        log.info("User logged in: {}", user.getUsername());

        String token = generateBearerToken(user.getId());

        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .accessToken(token)
                .tokenExpiresIn(TOKEN_EXPIRY_MS)
                .tokenType("Bearer")
                .message("Login successful")
                .preferencesJson(user.getPreferencesJson())
                .build();
    }

    @Transactional
    @SuppressWarnings("null")
    public void logout(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        log.info("User logout: {}", user.getUsername());
    }

    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("null")
    public void deleteUser(String userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Failed deletion attempt - invalid password for user: {}", user.getUsername());
            throw new IllegalArgumentException("Invalid password");
        }
        
        log.info("User deletion initiated: {}", user.getUsername());
        
        deleteAllUserData(userId);
        
        log.info("User and all related data deleted: {}", user.getUsername());
    }

    @SuppressWarnings("null")
    private void deleteAllUserData(String userId) {
        try {
            List<SudokuGameSession> allSessions = sudokuGameSessionRepository.findByUserId(userId);
            
            if (!allSessions.isEmpty()) {
                log.info("Cleaning up {} session(s) for user: {}", allSessions.size(), userId);
                
                for (SudokuGameSession session : allSessions) {
                    String sessionId = session.getSessionId();
                    Long puzzleId = session.getPuzzle() != null ? session.getPuzzle().getId() : null;
                    
                    try {
                        sudokuGameSessionRepository.deleteById(sessionId);
                        log.debug("Deleted session: {}", sessionId);
                        
                        if (puzzleId != null) {
                            sudokuPuzzleRepository.deleteById(puzzleId);
                            log.debug("Deleted puzzle {} for session: {}", puzzleId, sessionId);
                        }
                    } catch (Exception e) {
                        log.error("Failed to cleanup session {}: {}", sessionId, e.getMessage(), e);
                        throw new RuntimeException("Failed to cleanup session: " + sessionId, e);
                    }
                }
                log.info("All sessions cleaned up");
            }
        } catch (Exception e) {
            log.error("Critical error during session cleanup: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to cleanup sessions during account deletion", e);
        }
        
        gameScoreRepository.deleteByUserId(userId);
        log.debug("Game scores deleted");
        
        userRepository.deleteById(userId);
    }

    @Transactional
    @SuppressWarnings("null")
    public void deactivateUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        user.setIsActive(false);
        userRepository.save(user);
        log.info("User account deactivated: {}", user.getUsername());
    }

    @SuppressWarnings("null")
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public boolean validateCredentials(String emailOrUsername, String password) {
        Optional<User> userOpt = userRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        return user.getIsActive() && passwordEncoder.matches(password, user.getPasswordHash());
    }

    @Transactional
    @SuppressWarnings("null")
    public String updatePreferences(String userId, String preferencesJson) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setPreferencesJson(preferencesJson);
        userRepository.save(user);
        log.info("Preferences updated for user: {}", user.getUsername());
        return user.getPreferencesJson();
    }

    @SuppressWarnings("null")
    public String getPreferences(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return user.getPreferencesJson();
    }
}
