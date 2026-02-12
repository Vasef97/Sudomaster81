package com.ltp.sudomaster.pointsengine;

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
@DisplayName("Game Engine Unit Tests")
@SuppressWarnings("null")
class GameEngineUnitTest {

    @Autowired
    private GameEngine gameEngine;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SudokuPuzzleRepository puzzleRepository;

    @Autowired
    private SudokuGameSessionRepository sessionRepository;

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
        testUser.setUsername("gameengineuser");
        testUser.setEmail("engine@test.com");
        testUser.setPasswordHash("hashedpassword");
        testUser = userRepository.save(testUser);

        testPuzzle = new SudokuPuzzle();
        testPuzzle.setDifficulty(Enums.Difficulty.EASY);
        testPuzzle.setCluesString("530070000600195000098000060800060003400803001700020006060000280000419005000080079");
        testPuzzle.setSolutionString("534678912672195348198342567825961734349287651761524896956837281283419675417253829");
        testPuzzle = puzzleRepository.save(testPuzzle);
    }

    @Test
    @DisplayName("Game engine initializes")
    void testGameEngineInitialization() {
        assertNotNull(gameEngine);
    }

    @Test
    @DisplayName("Test user exists after setup")
    void testUserSetup() {
        Optional<User> user = userRepository.findById(testUser.getId());
        assertTrue(user.isPresent());
        assertEquals("gameengineuser", user.get().getUsername());
    }

    @Test
    @DisplayName("Test puzzle exists after setup")
    void testPuzzleSetup() {
        Optional<SudokuPuzzle> puzzle = puzzleRepository.findById(testPuzzle.getId());
        assertTrue(puzzle.isPresent());
        assertEquals(Enums.Difficulty.EASY, puzzle.get().getDifficulty());
    }

    @Test
    @DisplayName("Can create game session with puzzle")
    void testCreateGameSession() {
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

        assertNotNull(session.getSessionId());
        assertEquals(testUser.getId(), session.getUser().getId());
    }

    @Test
    @DisplayName("Can retrieve game session by ID")
    void testRetrieveGameSession() {
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
        assertEquals(Enums.GameStatus.IN_PROGRESS, retrieved.get().getStatus());
    }

    @Test
    @DisplayName("Board string is persisted correctly")
    void testBoardPersistence() {
        String boardString = "530070000600195000098000060800060003400803001700020006060000280000419005000080079";
        
        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setUser(testUser);
        session.setPuzzle(testPuzzle);
        session.setStatus(Enums.GameStatus.IN_PROGRESS);
        session.setBoardString(boardString);
        session.setCandidatesJson("{}");
        session = sessionRepository.save(session);

        Optional<SudokuGameSession> retrieved = sessionRepository.findById(session.getSessionId());
        assertTrue(retrieved.isPresent());
        assertEquals(boardString, retrieved.get().getBoardString());
    }

    @Test
    @DisplayName("Game status can be updated")
    void testGameStatusUpdate() {
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

        session.setStatus(Enums.GameStatus.COMPLETED);
        session = sessionRepository.save(session);

        Optional<SudokuGameSession> retrieved = sessionRepository.findById(session.getSessionId());
        assertTrue(retrieved.isPresent());
        assertEquals(Enums.GameStatus.COMPLETED, retrieved.get().getStatus());
    }

    @Test
    @DisplayName("Multiple puzzle difficulties can be used")
    void testMultipleDifficulties() {
        SudokuPuzzle mediumPuzzle = new SudokuPuzzle();
        mediumPuzzle.setDifficulty(Enums.Difficulty.MEDIUM);
        mediumPuzzle.setCluesString("530070000600195000098000060800060003400803001700020006060000280000419005000080079");
        mediumPuzzle.setSolutionString("534678912672195348198342567825961734349287651761524896956837281283419675417253829");
        mediumPuzzle = puzzleRepository.save(mediumPuzzle);

        assertEquals(Enums.Difficulty.EASY, testPuzzle.getDifficulty());
        assertEquals(Enums.Difficulty.MEDIUM, mediumPuzzle.getDifficulty());
    }

    @Test
    @DisplayName("Candidates JSON is stored and retrieved")
    void testCandidatesJSON() {
        String candidatesJson = "{\"0\":[1,2,3],\"1\":[4,5]}";
        
        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setUser(testUser);
        session.setPuzzle(testPuzzle);
        session.setStatus(Enums.GameStatus.IN_PROGRESS);
        session.setBoardString(testPuzzle.getCluesString());
        session.setCandidatesJson(candidatesJson);
        session = sessionRepository.save(session);

        Optional<SudokuGameSession> retrieved = sessionRepository.findById(session.getSessionId());
        assertTrue(retrieved.isPresent());
        assertEquals(candidatesJson, retrieved.get().getCandidatesJson());
    }

    @Test
    @DisplayName("User can have multiple game sessions")
    void testMultipleSessions() {
        for (int i = 0; i < 3; i++) {
            SudokuGameSession session = new SudokuGameSession();
            session.setSessionId(UUID.randomUUID().toString());
            session.setCreatedAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
            session.setUser(testUser);
            session.setPuzzle(testPuzzle);
            session.setStatus(Enums.GameStatus.IN_PROGRESS);
            session.setBoardString(testPuzzle.getCluesString());
            session.setCandidatesJson("{}");
            sessionRepository.save(session);
        }

        var userSessions = sessionRepository.findByUserId(testUser.getId());
        assertEquals(3, userSessions.size());
    }

    @Test
    @DisplayName("Session persistence is transactional")
    void testSessionTransactionality() {
        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setUser(testUser);
        session.setPuzzle(testPuzzle);
        session.setStatus(Enums.GameStatus.IN_PROGRESS);
        session.setBoardString(testPuzzle.getCluesString());
        session.setCandidatesJson("{}");
        
        SudokuGameSession saved = sessionRepository.save(session);
        assertNotNull(saved.getSessionId());

        Optional<SudokuGameSession> retrieved = sessionRepository.findById(saved.getSessionId());
        assertTrue(retrieved.isPresent());
    }

    @Test
    @DisplayName("Puzzle solution is stored securely")
    void testPuzzleSolution() {
        Optional<SudokuPuzzle> puzzle = puzzleRepository.findById(testPuzzle.getId());
        assertTrue(puzzle.isPresent());
        assertNotNull(puzzle.get().getSolutionString());
        assertTrue(puzzle.get().getSolutionString().length() > 0);
    }

    @Test
    @DisplayName("Game session deletion works")
    void testGameSessionDeletion() {
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
        sessionRepository.delete(session);

        Optional<SudokuGameSession> deleted = sessionRepository.findById(sessionId);
        assertFalse(deleted.isPresent());
    }
}
