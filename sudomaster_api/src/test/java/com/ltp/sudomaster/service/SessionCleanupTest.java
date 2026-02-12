package com.ltp.sudomaster.service;

import com.ltp.sudomaster.entity.*;
import com.ltp.sudomaster.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Session Cleanup Service Tests")
@SuppressWarnings("null")
class SessionCleanupTest {

    @Autowired
    private SessionCleanupService sessionCleanupService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SudokuPuzzleRepository puzzleRepository;

    @Autowired
    private SudokuGameSessionRepository sessionRepository;

    @Autowired
    private GameScoreRepository gameScoreRepository;

    private User testUser;

    private static final String CLUES = "530070000600195000098000060800060003400803001700020006060000280000419005000080079";
    private static final String SOLUTION = "534678912672195348198342567825961734349287651761524896956837281283419675417253829";

    @BeforeEach
    void setup() {
        gameScoreRepository.deleteAll();
        sessionRepository.deleteAll();
        userRepository.deleteAll();
        puzzleRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("cleanupuser");
        testUser.setEmail("cleanup@test.com");
        testUser.setPasswordHash("hashedpassword");
        testUser = userRepository.save(testUser);
    }

    private SudokuPuzzle createPuzzle() {
        SudokuPuzzle puzzle = new SudokuPuzzle();
        puzzle.setDifficulty(Enums.Difficulty.EASY);
        puzzle.setCluesString(CLUES);
        puzzle.setSolutionString(SOLUTION);
        return puzzleRepository.save(puzzle);
    }

    private SudokuGameSession createSession(User user, SudokuPuzzle puzzle, Enums.GameStatus status, LocalDateTime updatedAt) {
        SudokuGameSession session = SudokuGameSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .user(user)
                .puzzle(puzzle)
                .boardString(CLUES)
                .candidatesJson("{}")
                .status(status)
                .createdAt(updatedAt)
                .updatedAt(updatedAt)
                .build();
        return sessionRepository.save(session);
    }

    @Test
    @DisplayName("Cleanup deletes stale IN_PROGRESS sessions older than 7 days")
    void testCleanupStaleInProgressSessions() {
        SudokuPuzzle puzzle = createPuzzle();
        LocalDateTime eightDaysAgo = LocalDateTime.now().minusDays(8);
        SudokuGameSession staleSession = createSession(testUser, puzzle, Enums.GameStatus.IN_PROGRESS, eightDaysAgo);

        String sessionId = staleSession.getSessionId();
        assertTrue(sessionRepository.findBySessionId(sessionId).isPresent(), "Session should exist before cleanup");

        sessionCleanupService.cleanupStaleSessions();

        assertFalse(sessionRepository.findBySessionId(sessionId).isPresent(), "Stale IN_PROGRESS session should be deleted after cleanup");
    }

    @Test
    @DisplayName("Cleanup deletes stale COMPLETED sessions older than 7 days")
    void testCleanupStaleCompletedSessions() {
        SudokuPuzzle puzzle = createPuzzle();
        LocalDateTime eightDaysAgo = LocalDateTime.now().minusDays(8);
        SudokuGameSession staleSession = createSession(testUser, puzzle, Enums.GameStatus.COMPLETED, eightDaysAgo);

        String sessionId = staleSession.getSessionId();
        assertTrue(sessionRepository.findBySessionId(sessionId).isPresent(), "Session should exist before cleanup");

        sessionCleanupService.cleanupStaleSessions();

        assertFalse(sessionRepository.findBySessionId(sessionId).isPresent(), "Stale COMPLETED session should be deleted after cleanup");
    }

    @Test
    @DisplayName("Recent sessions (less than 7 days old) are NOT cleaned up")
    void testRecentSessionsNotCleaned() {
        SudokuPuzzle puzzle = createPuzzle();
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        SudokuGameSession recentSession = createSession(testUser, puzzle, Enums.GameStatus.IN_PROGRESS, twoDaysAgo);

        String sessionId = recentSession.getSessionId();

        sessionCleanupService.cleanupStaleSessions();

        assertTrue(sessionRepository.findBySessionId(sessionId).isPresent(), "Recent session should NOT be deleted");
    }

    @Test
    @DisplayName("Cleanup deletes associated puzzle when session is cleaned")
    void testCleanupDeletesAssociatedPuzzles() {
        SudokuPuzzle puzzle = createPuzzle();
        Long puzzleId = puzzle.getId();
        LocalDateTime eightDaysAgo = LocalDateTime.now().minusDays(8);
        SudokuGameSession staleSession = createSession(testUser, puzzle, Enums.GameStatus.IN_PROGRESS, eightDaysAgo);

        String sessionId = staleSession.getSessionId();
        assertTrue(puzzleRepository.findById(puzzleId).isPresent(), "Puzzle should exist before cleanup");

        sessionCleanupService.cleanupStaleSessions();

        assertFalse(sessionRepository.findBySessionId(sessionId).isPresent(), "Session should be deleted");
        assertFalse(puzzleRepository.findById(puzzleId).isPresent(), "Associated puzzle should be deleted after cleanup");
    }

    @Test
    @DisplayName("Cleanup completes without errors when there are no stale sessions")
    void testCleanupWithNoStaleSessions() {
        SudokuPuzzle puzzle = createPuzzle();
        LocalDateTime now = LocalDateTime.now();
        SudokuGameSession recentSession = createSession(testUser, puzzle, Enums.GameStatus.IN_PROGRESS, now);

        String sessionId = recentSession.getSessionId();

        assertDoesNotThrow(() -> sessionCleanupService.cleanupStaleSessions(),
                "Cleanup should complete without errors even when no stale sessions exist");

        assertTrue(sessionRepository.findBySessionId(sessionId).isPresent(), "Recent session should remain untouched");
    }
}
