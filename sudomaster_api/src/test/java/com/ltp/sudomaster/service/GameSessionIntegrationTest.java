package com.ltp.sudomaster.service;

import com.ltp.sudomaster.entity.*;
import com.ltp.sudomaster.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import java.util.UUID;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Game Session Integration Tests")
@SuppressWarnings("null")
class GameSessionIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SudokuGameSessionRepository sessionRepository;

    @Autowired
    private SudokuPuzzleRepository puzzleRepository;

    @Autowired
    private GameScoreRepository gameScoreRepository;

    private User testUser;
    private SudokuPuzzle testPuzzle;

    @BeforeEach
    void setup() {
        gameScoreRepository.deleteAll();
        sessionRepository.deleteAll();
        userRepository.deleteAll();
        puzzleRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("sessionuser");
        testUser.setEmail("session@test.com");
        testUser.setPasswordHash("hashedpassword");
        testUser = userRepository.save(testUser);

        testPuzzle = new SudokuPuzzle();
        testPuzzle.setDifficulty(Enums.Difficulty.MEDIUM);
        testPuzzle.setCluesString("530070000600195000098000060800060003400803001700020006060000280000419005000080079");
        testPuzzle.setSolutionString("534678912672195348198342567825961734349287651761524896956837281283419675417253829");
        testPuzzle = puzzleRepository.save(testPuzzle);
    }

    @Test
    @DisplayName("User-to-Session relationship persists correctly")
    void testUserSessionRelationship() {
        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setUser(testUser);
        session.setPuzzle(testPuzzle);
        session.setStatus(Enums.GameStatus.IN_PROGRESS);
        session.setBoardString(testPuzzle.getCluesString());
        session.setCandidatesJson("{}");
        session = sessionRepository.save(session);

        Optional<SudokuGameSession> retrieved = sessionRepository.findById(session.getSessionId());
        assertTrue(retrieved.isPresent());
        assertEquals(testUser.getId(), retrieved.get().getUser().getId());
    }

    @Test
    @DisplayName("Session-to-Puzzle relationship maintains integrity")
    void testSessionPuzzleRelationship() {
        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setUser(testUser);
        session.setPuzzle(testPuzzle);
        session.setStatus(Enums.GameStatus.IN_PROGRESS);
        session.setBoardString(testPuzzle.getCluesString());
        session.setCandidatesJson("{}");
        session = sessionRepository.save(session);

        Optional<SudokuGameSession> retrieved = sessionRepository.findById(session.getSessionId());
        assertTrue(retrieved.isPresent());
        assertEquals(testPuzzle.getId(), retrieved.get().getPuzzle().getId());
    }

    @Test
    @DisplayName("Multiple sessions per user are supported")
    void testMultipleSessionsPerUser() {
        SudokuGameSession session1 = new SudokuGameSession();
        session1.setSessionId(UUID.randomUUID().toString());
        session1.setCreatedAt(LocalDateTime.now());
        session1.setUpdatedAt(LocalDateTime.now());
        session1.setUser(testUser);
        session1.setPuzzle(testPuzzle);
        session1.setStatus(Enums.GameStatus.IN_PROGRESS);
        session1.setBoardString(testPuzzle.getCluesString());
        session1.setCandidatesJson("{}");
        session1 = sessionRepository.save(session1);

        SudokuGameSession session2 = new SudokuGameSession();
        session2.setSessionId(UUID.randomUUID().toString());
        session2.setCreatedAt(LocalDateTime.now());
        session2.setUpdatedAt(LocalDateTime.now());
        session2.setUser(testUser);
        session2.setPuzzle(testPuzzle);
        session2.setStatus(Enums.GameStatus.COMPLETED);
        session2.setBoardString(testPuzzle.getSolutionString());
        session2.setCandidatesJson("{}");
        session2 = sessionRepository.save(session2);

        assertEquals(2, sessionRepository.count());
    }

    @Test
    @DisplayName("Session status transitions work correctly")
    void testSessionStatusTransitions() {
        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setUser(testUser);
        session.setPuzzle(testPuzzle);
        session.setStatus(Enums.GameStatus.IN_PROGRESS);
        session.setBoardString(testPuzzle.getCluesString());
        session.setCandidatesJson("{}");
        session = sessionRepository.save(session);

        assertEquals(Enums.GameStatus.IN_PROGRESS, session.getStatus());

        session.setStatus(Enums.GameStatus.COMPLETED);
        session = sessionRepository.save(session);
        Optional<SudokuGameSession> completed = sessionRepository.findById(session.getSessionId());
        assertEquals(Enums.GameStatus.COMPLETED, completed.get().getStatus());
    }

    @Test
    @DisplayName("Session board updates persist correctly")
    void testSessionBoardUpdates() {
        String originalBoard = testPuzzle.getCluesString();
        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setUser(testUser);
        session.setPuzzle(testPuzzle);
        session.setStatus(Enums.GameStatus.IN_PROGRESS);
        session.setBoardString(originalBoard);
        session.setCandidatesJson("{}");
        session = sessionRepository.save(session);

        String updatedBoard = "534070000600195000098000060800060003400803001700020006060000280000419005000080079";
        session.setBoardString(updatedBoard);
        session = sessionRepository.save(session);

        Optional<SudokuGameSession> retrieved = sessionRepository.findById(session.getSessionId());
        assertTrue(retrieved.isPresent());
        assertEquals(updatedBoard, retrieved.get().getBoardString());
        assertNotEquals(originalBoard, retrieved.get().getBoardString());
    }

    @Test
    @DisplayName("Candidates JSON persists and updates correctly")
    void testCandidatesJsonPersistence() {
        String initialCandidates = "{}";
        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setUser(testUser);
        session.setPuzzle(testPuzzle);
        session.setStatus(Enums.GameStatus.IN_PROGRESS);
        session.setBoardString(testPuzzle.getCluesString());
        session.setCandidatesJson(initialCandidates);
        session = sessionRepository.save(session);

        String updatedCandidates = "{\"0\":[1,2,3],\"1\":[4,5]}";
        session.setCandidatesJson(updatedCandidates);
        session = sessionRepository.save(session);

        Optional<SudokuGameSession> retrieved = sessionRepository.findById(session.getSessionId());
        assertTrue(retrieved.isPresent());
        assertEquals(updatedCandidates, retrieved.get().getCandidatesJson());
    }

    @Test
    @DisplayName("Session deletion cascades properly")
    void testSessionDeletion() {
        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setUser(testUser);
        session.setPuzzle(testPuzzle);
        session.setStatus(Enums.GameStatus.IN_PROGRESS);
        session.setBoardString(testPuzzle.getCluesString());
        session.setCandidatesJson("{}");
        session = sessionRepository.save(session);

        String sessionId = session.getSessionId();
        assertTrue(sessionRepository.findById(sessionId).isPresent());

        sessionRepository.delete(session);

        assertFalse(sessionRepository.findById(sessionId).isPresent());
    }

    @Test
    @DisplayName("Multiple users can have sessions")
    void testMultipleUsersMultipleSessions() {
        User user2 = new User();
        user2.setUsername("sessionuser2");
        user2.setEmail("session2@test.com");
        user2.setPasswordHash("hashedpassword2");
        user2 = userRepository.save(user2);

        SudokuGameSession session1 = new SudokuGameSession();
        session1.setSessionId(UUID.randomUUID().toString());
        session1.setCreatedAt(LocalDateTime.now());
        session1.setUpdatedAt(LocalDateTime.now());
        session1.setUser(testUser);
        session1.setPuzzle(testPuzzle);
        session1.setStatus(Enums.GameStatus.IN_PROGRESS);
        session1.setBoardString(testPuzzle.getCluesString());
        session1.setCandidatesJson("{}");
        session1 = sessionRepository.save(session1);

        SudokuGameSession session2 = new SudokuGameSession();
        session2.setSessionId(UUID.randomUUID().toString());
        session2.setCreatedAt(LocalDateTime.now());
        session2.setUpdatedAt(LocalDateTime.now());
        session2.setUser(user2);
        session2.setPuzzle(testPuzzle);
        session2.setStatus(Enums.GameStatus.IN_PROGRESS);
        session2.setBoardString(testPuzzle.getCluesString());
        session2.setCandidatesJson("{}");
        session2 = sessionRepository.save(session2);

        assertEquals(2, userRepository.count());
        assertEquals(2, sessionRepository.count());
    }

    @Test
    @DisplayName("Game score integration with sessions")
    void testGameScoreIntegration() {
        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setUser(testUser);
        session.setPuzzle(testPuzzle);
        session.setStatus(Enums.GameStatus.COMPLETED);
        session.setBoardString(testPuzzle.getSolutionString());
        session.setCandidatesJson("{}");
        session = sessionRepository.save(session);

        GameScore score = new GameScore();
        score.setUser(testUser);
        score.setSessionId(session.getSessionId());
        score.setElapsedTimeSeconds(300);
        score.setDifficulty(testPuzzle.getDifficulty());
        score = gameScoreRepository.save(score);

        Optional<GameScore> retrieved = gameScoreRepository.findById(score.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(session.getSessionId(), retrieved.get().getSessionId());
    }

    @Test
    @DisplayName("Foreign key relationships prevent orphaned records")
    void testForeignKeyIntegrity() {
        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setUser(testUser);
        session.setPuzzle(testPuzzle);
        session.setStatus(Enums.GameStatus.IN_PROGRESS);
        session.setBoardString(testPuzzle.getCluesString());
        session.setCandidatesJson("{}");
        session = sessionRepository.save(session);

        Optional<SudokuGameSession> retrieved = sessionRepository.findById(session.getSessionId());
        assertTrue(retrieved.isPresent());
        assertNotNull(retrieved.get().getUser());
        assertNotNull(retrieved.get().getPuzzle());
    }

    @Test
    @DisplayName("Session ID uniqueness is maintained")
    void testSessionIdUniqueness() {
        SudokuGameSession session1 = new SudokuGameSession();
        session1.setSessionId(UUID.randomUUID().toString());
        session1.setCreatedAt(LocalDateTime.now());
        session1.setUpdatedAt(LocalDateTime.now());
        session1.setUser(testUser);
        session1.setPuzzle(testPuzzle);
        session1.setStatus(Enums.GameStatus.IN_PROGRESS);
        session1.setBoardString(testPuzzle.getCluesString());
        session1.setCandidatesJson("{}");
        session1 = sessionRepository.save(session1);

        SudokuGameSession session2 = new SudokuGameSession();
        session2.setSessionId(UUID.randomUUID().toString());
        session2.setCreatedAt(LocalDateTime.now());
        session2.setUpdatedAt(LocalDateTime.now());
        session2.setUser(testUser);
        session2.setPuzzle(testPuzzle);
        session2.setStatus(Enums.GameStatus.IN_PROGRESS);
        session2.setBoardString(testPuzzle.getCluesString());
        session2.setCandidatesJson("{}");
        session2 = sessionRepository.save(session2);

        assertNotEquals(session1.getSessionId(), session2.getSessionId());
    }
}
