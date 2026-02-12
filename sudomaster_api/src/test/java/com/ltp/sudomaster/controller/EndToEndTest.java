package com.ltp.sudomaster.controller;

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
@DisplayName("End-to-End Workflow Tests")
@SuppressWarnings("null")
class EndToEndTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SudokuGameSessionRepository sessionRepository;

    @Autowired
    private SudokuPuzzleRepository puzzleRepository;

    @Autowired
    private GameScoreRepository gameScoreRepository;

    private SudokuPuzzle testPuzzle;

    @BeforeEach
    void setup() {
        gameScoreRepository.deleteAll();
        sessionRepository.deleteAll();
        userRepository.deleteAll();
        puzzleRepository.deleteAll();

        testPuzzle = new SudokuPuzzle();
        testPuzzle.setDifficulty(Enums.Difficulty.EASY);
        testPuzzle.setCluesString("530070000600195000098000060800060003400803001700020006060000280000419005000080079");
        testPuzzle.setSolutionString("534678912672195348198342567825961734349287651761524896956837281283419675417253829");
        testPuzzle = puzzleRepository.save(testPuzzle);
    }

    @Test
    @DisplayName("User can create account and access system")
    void testUserAccountCreation() {
        User user = new User();
        user.setUsername("e2euser1");
        user.setEmail("e2euser1@test.com");
        user.setPasswordHash("hashedpassword");
        user = userRepository.save(user);

        Optional<User> retrieved = userRepository.findByUsername("e2euser1");
        assertTrue(retrieved.isPresent());
        assertEquals("e2euser1@test.com", retrieved.get().getEmail());
    }

    @Test
    @DisplayName("User can create game after account creation")
    void testCreateGameAfterAccountCreation() {
        User user = new User();
        user.setUsername("e2euser2");
        user.setEmail("e2euser2@test.com");
        user.setPasswordHash("hashedpassword");
        user = userRepository.save(user);

        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setUser(user);
        session.setPuzzle(testPuzzle);
        session.setStatus(Enums.GameStatus.IN_PROGRESS);
        session.setBoardString(testPuzzle.getCluesString());
        session.setCandidatesJson("{}");
        session = sessionRepository.save(session);

        assertTrue(sessionRepository.count() > 0);
    }

    @Test
    @DisplayName("User can play and complete game")
    void testPlayAndPauseGame() {
        User user = new User();
        user.setUsername("e2euser3");
        user.setEmail("e2euser3@test.com");
        user.setPasswordHash("hashedpassword");
        user = userRepository.save(user);

        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setUser(user);
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
    @DisplayName("User can complete game and record score")
    void testCompleteGameAndRecordScore() {
        User user = new User();
        user.setUsername("e2euser4");
        user.setEmail("e2euser4@test.com");
        user.setPasswordHash("hashedpassword");
        user = userRepository.save(user);

        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setUser(user);
        session.setPuzzle(testPuzzle);
        session.setStatus(Enums.GameStatus.IN_PROGRESS);
        session.setBoardString(testPuzzle.getCluesString());
        session.setCandidatesJson("{}");
        session = sessionRepository.save(session);

        session.setBoardString(testPuzzle.getSolutionString());
        session.setStatus(Enums.GameStatus.COMPLETED);
        session = sessionRepository.save(session);

        GameScore score = new GameScore();
        score.setUser(user);
        score.setSessionId(session.getSessionId());
        score.setElapsedTimeSeconds(300);
        score.setDifficulty(testPuzzle.getDifficulty());
        score = gameScoreRepository.save(score);

        assertTrue(gameScoreRepository.count() > 0);
    }

    @Test
    @DisplayName("User can play multiple games")
    void testMultipleGamePlay() {
        User user = new User();
        user.setUsername("e2euser5");
        user.setEmail("e2euser5@test.com");
        user.setPasswordHash("hashedpassword");
        user = userRepository.save(user);

        for (int i = 0; i < 3; i++) {
            SudokuGameSession session = new SudokuGameSession();
            session.setSessionId(UUID.randomUUID().toString());
            session.setCreatedAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
            session.setUser(user);
            session.setPuzzle(testPuzzle);
            session.setStatus(Enums.GameStatus.IN_PROGRESS);
            session.setBoardString(testPuzzle.getCluesString());
            session.setCandidatesJson("{}");
            sessionRepository.save(session);
        }

        assertEquals(3, sessionRepository.count());
    }

    @Test
    @DisplayName("Multiple users can have separate game states")
    void testMultipleUsersIndependentGames() {
        User user1 = new User();
        user1.setUsername("e2euser6");
        user1.setEmail("e2euser6@test.com");
        user1.setPasswordHash("hashedpassword");
        user1 = userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("e2euser7");
        user2.setEmail("e2euser7@test.com");
        user2.setPasswordHash("hashedpassword");
        user2 = userRepository.save(user2);

        SudokuGameSession session1 = new SudokuGameSession();
        session1.setSessionId(UUID.randomUUID().toString());
        session1.setCreatedAt(LocalDateTime.now());
        session1.setUpdatedAt(LocalDateTime.now());
        session1.setUser(user1);
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
        session2.setStatus(Enums.GameStatus.COMPLETED);
        session2.setBoardString(testPuzzle.getSolutionString());
        session2.setCandidatesJson("{}");
        session2 = sessionRepository.save(session2);

        assertEquals(Enums.GameStatus.IN_PROGRESS, session1.getStatus());
        assertEquals(Enums.GameStatus.COMPLETED, session2.getStatus());
    }
}
