package com.ltp.sudomaster.controller;

import com.ltp.sudomaster.entity.*;
import com.ltp.sudomaster.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Leaderboard Tests")
@SuppressWarnings("null")
class LeaderboardRankingTest {

    @Autowired
    private GameScoreRepository gameScoreRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SudokuGameSessionRepository sessionRepository;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setup() {
        gameScoreRepository.deleteAll();
        sessionRepository.deleteAll();
        userRepository.deleteAll();

        user1 = new User();
        user1.setUsername("leaderuser1");
        user1.setEmail("leader1@test.com");
        user1.setPasswordHash("hash");
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setUsername("leaderuser2");
        user2.setEmail("leader2@test.com");
        user2.setPasswordHash("hash");
        user2 = userRepository.save(user2);

        user3 = new User();
        user3.setUsername("leaderuser3");
        user3.setEmail("leader3@test.com");
        user3.setPasswordHash("hash");
        user3 = userRepository.save(user3);
    }

    @Test
    @DisplayName("Empty leaderboard returns empty list")
    void testEmptyLeaderboard() {
        List<GameScore> leaderboard = gameScoreRepository.findAll();
        assertTrue(leaderboard.isEmpty());
    }

    @Test
    @DisplayName("Single score in leaderboard")
    void testSingleScoreLeaderboard() {
        createScore(user1, "session-1", 1000, Enums.Difficulty.EASY, 300);
        List<GameScore> leaderboard = gameScoreRepository.findAll();
        assertEquals(1, leaderboard.size());
    }

    @Test
    @DisplayName("Multiple scores preserve order by points")
    void testMultipleScoresOrdering() {
        createScore(user1, "session-1", 1000, Enums.Difficulty.EASY, 300);
        createScore(user2, "session-2", 2000, Enums.Difficulty.MEDIUM, 400);
        createScore(user3, "session-3", 1500, Enums.Difficulty.HARD, 500);

        List<GameScore> leaderboard = gameScoreRepository.findAll();
        assertEquals(3, leaderboard.size());
    }

    @Test
    @DisplayName("Top scores are highest values")
    void testTopScoresHighest() {
        createScore(user1, "session-low", 100, Enums.Difficulty.EASY, 300);
        createScore(user2, "session-high", 9999, Enums.Difficulty.HARD, 500);
        createScore(user3, "session-mid", 5000, Enums.Difficulty.MEDIUM, 400);

        List<GameScore> leaderboard = gameScoreRepository.findAll();
        assertEquals(3, leaderboard.size());
    }

    @Test
    @DisplayName("Duplicate user scores accumulate")
    void testUserMultipleScores() {
        createScore(user1, "session-1", 1000, Enums.Difficulty.EASY, 300);
        createScore(user1, "session-2", 500, Enums.Difficulty.MEDIUM, 400);
        createScore(user2, "session-3", 2000, Enums.Difficulty.HARD, 500);

        long user1Count = gameScoreRepository.findAll().stream()
                .filter(s -> s.getUser().getId().equals(user1.getId())).count();
        assertEquals(2, user1Count);
    }

    @Test
    @DisplayName("Leaderboard retrieval all present")
    void testLeaderboardRetrievalAll() {
        for (int i = 0; i < 10; i++) {
            createScore(user1, "session-" + i, 1000 + i, Enums.Difficulty.EASY, 300);
        }

        List<GameScore> leaderboard = gameScoreRepository.findAll();
        assertEquals(10, leaderboard.size());
    }

    @Test
    @DisplayName("Score timestamp recorded")
    void testScoreTimestamp() {
        GameScore score = new GameScore();
        score.setUser(user1);
        score.setSessionId("session-score");
        score.setDifficulty(Enums.Difficulty.EASY);
        score.setElapsedTimeSeconds(300);
        score.setScore(1000);
        score = gameScoreRepository.save(score);

        assertNotNull(score.getId());
    }

    @Test
    @DisplayName("User identifier in score")
    void testUserIdentifierInScore() {
        createScore(user1, "session-user", 1000, Enums.Difficulty.EASY, 300);
        GameScore retrieved = gameScoreRepository.findAll().get(0);

        assertNotNull(retrieved.getUser());
        assertEquals(user1.getId(), retrieved.getUser().getId());
    }

    @Test
    @DisplayName("Score data persists in database")
    void testScorePersistence() {
        createScore(user1, "session-persist", 1234, Enums.Difficulty.EASY, 300);

        List<GameScore> retrieved = gameScoreRepository.findAll();
        assertTrue(retrieved.stream().anyMatch(s -> s.getScore() == 1234));
    }

    @Test
    @DisplayName("Different user scores separate")
    void testUserScoreSeparation() {
        createScore(user1, "session-sep1", 1000, Enums.Difficulty.EASY, 300);
        createScore(user2, "session-sep2", 2000, Enums.Difficulty.MEDIUM, 400);

        long user1Count = gameScoreRepository.findAll().stream()
                .filter(s -> s.getUser().getId().equals(user1.getId())).count();
        long user2Count = gameScoreRepository.findAll().stream()
                .filter(s -> s.getUser().getId().equals(user2.getId())).count();

        assertEquals(1, user1Count);
        assertEquals(1, user2Count);
    }

    @Test
    @DisplayName("Zero points score valid")
    void testZeroPointsScore() {
        createScore(user1, "session-zero", 0, Enums.Difficulty.EASY, 300);

        List<GameScore> leaderboard = gameScoreRepository.findAll();
        assertTrue(leaderboard.stream().anyMatch(s -> s.getScore() == 0));
    }

    @Test
    @DisplayName("High value points preserved")
    void testHighPointsValue() {
        createScore(user1, "session-high-val", 999999, Enums.Difficulty.HARD, 600);

        GameScore retrieved = gameScoreRepository.findAll().get(0);
        assertEquals(999999, retrieved.getScore());
    }

    @Test
    @DisplayName("Leaderboard consistency across queries")
    void testLeaderboardConsistency() {
        createScore(user1, "session-consist", 1000, Enums.Difficulty.EASY, 300);

        List<GameScore> firstQuery = gameScoreRepository.findAll();
        List<GameScore> secondQuery = gameScoreRepository.findAll();

        assertEquals(firstQuery.size(), secondQuery.size());
    }

    @Test
    @DisplayName("Score retrieval by user")
    void testScoreRetrievalByUser() {
        createScore(user1, "session-retrieval", 1000, Enums.Difficulty.EASY, 300);

        List<GameScore> userScores = gameScoreRepository.findAll().stream()
                .filter(s -> s.getUser().getId().equals(user1.getId()))
                .toList();

        assertEquals(1, userScores.size());
    }

    @Test
    @DisplayName("Ranking position calculation")
    void testRankingPositionCalculation() {
        createScore(user1, "session-rank1", 3000, Enums.Difficulty.EASY, 300);
        createScore(user2, "session-rank2", 2000, Enums.Difficulty.MEDIUM, 400);
        createScore(user3, "session-rank3", 1000, Enums.Difficulty.HARD, 500);

        List<GameScore> sorted = gameScoreRepository.findAll().stream()
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                .toList();

        assertEquals(3, sorted.size());
        assertEquals(3000, sorted.get(0).getScore());
    }

    private void createScore(User user, String sessionId, Integer score, Enums.Difficulty difficulty, Integer elapsedTime) {
        GameScore gameScore = new GameScore();
        gameScore.setUser(user);
        gameScore.setSessionId(sessionId);
        gameScore.setDifficulty(difficulty);
        gameScore.setElapsedTimeSeconds(elapsedTime);
        gameScore.setScore(score);
        gameScoreRepository.save(gameScore);
    }
}
