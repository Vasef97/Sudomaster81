package com.ltp.sudomaster.pointsengine;

import com.ltp.sudomaster.entity.*;
import com.ltp.sudomaster.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import java.util.UUID;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Performance Tests")
@SuppressWarnings("null")
class PerformanceTest {

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
        testPuzzle.setDifficulty(Enums.Difficulty.MEDIUM);
        testPuzzle.setCluesString("530070000600195000098000060800060003400803001700020006060000280000419005000080079");
        testPuzzle.setSolutionString("534678912672195348198342567825961734349287651761524896956837281283419675417253829");
        testPuzzle = puzzleRepository.save(testPuzzle);
    }

    @Test
    @DisplayName("Bulk user insertion performance")
    void testBulkUserInsertion() {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            User user = new User();
            user.setUsername("perfuser" + i);
            user.setEmail("perf" + i + "@test.com");
            user.setPasswordHash("hashedpassword");
            userRepository.save(user);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertEquals(100, userRepository.count());
        assertTrue(duration < 5000);
    }

    @Test
    @DisplayName("Single user retrieval is fast")
    void testSingleUserRetrieval() {
        User user = new User();
        user.setUsername("retrievaluser");
        user.setEmail("retrieval@test.com");
        user.setPasswordHash("hashedpassword");
        userRepository.save(user);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            userRepository.findByUsername("retrievaluser");
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 1000);
    }

    @Test
    @DisplayName("Multiple session creation is efficient")
    void testMultipleSessionCreation() {
        User user = new User();
        user.setUsername("sessionperfuser");
        user.setEmail("sessionperf@test.com");
        user.setPasswordHash("hashedpassword");
        user = userRepository.save(user);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 50; i++) {
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

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertEquals(50, sessionRepository.count());
        assertTrue(duration < 3000);
    }

    @Test
    @DisplayName("Session retrieval by ID is fast")
    void testSessionRetrievalById() {
        User user = new User();
        user.setUsername("retrievalsessionuser");
        user.setEmail("retrievalsession@test.com");
        user.setPasswordHash("hashedpassword");
        user = userRepository.save(user);

        List<String> sessionIds = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
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
            sessionIds.add(session.getSessionId());
        }

        long startTime = System.currentTimeMillis();

        for (String sessionId : sessionIds) {
            sessionRepository.findById(sessionId);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 500);
    }

    @Test
    @DisplayName("Bulk game score creation")
    void testBulkGameScoreCreation() {
        User user = new User();
        user.setUsername("scoreuser");
        user.setEmail("score@test.com");
        user.setPasswordHash("hashedpassword");
        user = userRepository.save(user);

        List<SudokuGameSession> sessions = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            SudokuGameSession session = new SudokuGameSession();
            session.setSessionId(UUID.randomUUID().toString());
            session.setCreatedAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
            session.setUser(user);
            session.setPuzzle(testPuzzle);
            session.setStatus(Enums.GameStatus.COMPLETED);
            session.setBoardString(testPuzzle.getSolutionString());
            session.setCandidatesJson("{}");
            session = sessionRepository.save(session);
            sessions.add(session);
        }

        long startTime = System.currentTimeMillis();

        for (SudokuGameSession session : sessions) {
            GameScore score = new GameScore();
            score.setUser(user);
            score.setSessionId(session.getSessionId());
            score.setElapsedTimeSeconds(300);
            score.setDifficulty(testPuzzle.getDifficulty());
            gameScoreRepository.save(score);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertEquals(20, gameScoreRepository.count());
        assertTrue(duration < 2000);
    }

    @Test
    @DisplayName("Count queries are efficient")
    void testCountQueryEfficiency() {
        for (int i = 0; i < 30; i++) {
            User user = new User();
            user.setUsername("countuser" + i);
            user.setEmail("count" + i + "@test.com");
            user.setPasswordHash("hashedpassword");
            userRepository.save(user);
        }

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            userRepository.count();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 1000);
    }

    @Test
    @DisplayName("Board string persistence is fast")
    void testBoardStringPersistence() {
        User user = new User();
        user.setUsername("boarduser");
        user.setEmail("board@test.com");
        user.setPasswordHash("hashedpassword");
        user = userRepository.save(user);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 30; i++) {
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

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 2000);
    }

    @Test
    @DisplayName("Large candidates JSON handling")
    void testLargeCandidatesJsonHandling() {
        User user = new User();
        user.setUsername("candidatesuser");
        user.setEmail("candidates@test.com");
        user.setPasswordHash("hashedpassword");
        user = userRepository.save(user);

        StringBuilder largeJson = new StringBuilder("{");
        for (int i = 0; i < 81; i++) {
            if (i > 0) largeJson.append(",");
            largeJson.append("\"").append(i).append("\":[1,2,3,4,5]");
        }
        largeJson.append("}");

        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setUser(user);
        session.setPuzzle(testPuzzle);
        session.setStatus(Enums.GameStatus.IN_PROGRESS);
        session.setBoardString(testPuzzle.getCluesString());
        session.setCandidatesJson(largeJson.toString());
        session = sessionRepository.save(session);

        assertNotNull(session.getCandidatesJson());
        assertTrue(session.getCandidatesJson().contains("1,2,3,4,5"));
    }

    @Test
    @DisplayName("Multiple concurrent users simultaneous operations")
    void testMultipleConcurrentUsers() {
        long startTime = System.currentTimeMillis();

        for (int u = 0; u < 20; u++) {
            User user = new User();
            user.setUsername("concurrentuser" + u);
            user.setEmail("concurrent" + u + "@test.com");
            user.setPasswordHash("hashedpassword");
            user = userRepository.save(user);

            for (int s = 0; s < 5; s++) {
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
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertEquals(20, userRepository.count());
        assertEquals(100, sessionRepository.count());
        assertTrue(duration < 5000);
    }

    @Test
    @DisplayName("Update operations maintain performance")
    void testUpdateOperationsPerformance() {
        User user = new User();
        user.setUsername("updateuser");
        user.setEmail("update@test.com");
        user.setPasswordHash("hashedpassword");
        user = userRepository.save(user);

        List<SudokuGameSession> sessions = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
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
            sessions.add(session);
        }

        long startTime = System.currentTimeMillis();

        for (SudokuGameSession session : sessions) {
            session.setStatus(Enums.GameStatus.COMPLETED);
            session.setBoardString(testPuzzle.getSolutionString());
            sessionRepository.save(session);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 2000);
    }

    @Test
    @DisplayName("Deletion operations are efficient")
    void testDeletionPerformance() {
        User user = new User();
        user.setUsername("deleteuser");
        user.setEmail("delete@test.com");
        user.setPasswordHash("hashedpassword");
        user = userRepository.save(user);

        for (int i = 0; i < 25; i++) {
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

        long startTime = System.currentTimeMillis();

        sessionRepository.deleteAll();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertEquals(0, sessionRepository.count());
        assertTrue(duration < 2000);
    }
}
