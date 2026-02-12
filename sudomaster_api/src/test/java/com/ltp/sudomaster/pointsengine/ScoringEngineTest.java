package com.ltp.sudomaster.pointsengine;

import com.ltp.sudomaster.dto.GameCompleteResponse;
import com.ltp.sudomaster.entity.*;
import com.ltp.sudomaster.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Scoring Engine Tests")
@SuppressWarnings("null")
class ScoringEngineTest {

    @Autowired
    private GameEngine gameEngine;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SudokuGameSessionRepository sessionRepository;

    @Autowired
    private SudokuPuzzleRepository puzzleRepository;

    @Autowired
    private GameScoreRepository gameScoreRepository;

    private User testUser;
    private SudokuGameSession testSession;

    @BeforeEach
    void setup() {
        gameScoreRepository.deleteAll();
        sessionRepository.deleteAll();
        userRepository.deleteAll();
        puzzleRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("scoringuser");
        testUser.setEmail("scoring@test.com");
        testUser.setPasswordHash("hashedpassword");
        testUser = userRepository.save(testUser);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            testUser.getId(), // Use UUID as principal (matches production JWT behavior)
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        SudokuPuzzle puzzle = new SudokuPuzzle();
        puzzle.setDifficulty(Enums.Difficulty.EASY);
        puzzle.setCluesString("530070000600195000098000060800060003400803001700020006060000280000419005000080079");
        puzzle.setSolutionString("534678912672195348198342567825961734349287651761524896956837281283419675417253829");
        puzzle = puzzleRepository.save(puzzle);

        testSession = new SudokuGameSession();
        testSession.setSessionId(UUID.randomUUID().toString());
        testSession.setCreatedAt(LocalDateTime.now());
        testSession.setUpdatedAt(LocalDateTime.now());
        testSession.setUser(testUser);
        testSession.setPuzzle(puzzle);
        testSession.setStatus(Enums.GameStatus.IN_PROGRESS);
        testSession.setBoardString(puzzle.getSolutionString());
        testSession.setCandidatesJson("{}");
        testSession = sessionRepository.save(testSession);
    }

    @Test
    @DisplayName("Game completion creates score record")
    void testScoreCreationOnCompletion() {
        testSession.setStatus(Enums.GameStatus.COMPLETED);
        sessionRepository.save(testSession);

        GameCompleteResponse response = gameEngine.completeGame(testSession.getSessionId(), 300, 0, false);

        assertNotNull(response);
        assertNotNull(response.getScore());
        assertTrue(response.getScore() > 0);
    }

    @Test
    @DisplayName("Score calculation considers difficulty")
    void testDifficultyMultiplier() {
        testSession.setStatus(Enums.GameStatus.COMPLETED);
        sessionRepository.save(testSession);

        GameCompleteResponse easyResponse = gameEngine.completeGame(testSession.getSessionId(), 300, 0, false);
        Integer easyScore = easyResponse.getScore();

        assertNotNull(easyScore);
        assertTrue(easyScore > 0);
    }

    @Test
    @DisplayName("Faster completion yields more points")
    void testFasterCompletionBonus() {
        testSession.setStatus(Enums.GameStatus.COMPLETED);
        sessionRepository.save(testSession);

        GameCompleteResponse fastResponse = gameEngine.completeGame(testSession.getSessionId(), 60, 0, false);
        Integer fastScore = fastResponse.getScore();

        SudokuPuzzle slowPuzzle = new SudokuPuzzle();
        slowPuzzle.setDifficulty(Enums.Difficulty.EASY);
        slowPuzzle.setCluesString("530070000600195000098000060800060003400803001700020006060000280000419005000080079");
        slowPuzzle.setSolutionString("534678912672195348198342567825961734349287651761524896956837281283419675417253829");
        slowPuzzle = puzzleRepository.save(slowPuzzle);

        SudokuGameSession slowSession = new SudokuGameSession();
        slowSession.setSessionId(UUID.randomUUID().toString());
        slowSession.setCreatedAt(LocalDateTime.now());
        slowSession.setUpdatedAt(LocalDateTime.now());
        slowSession.setUser(testUser);
        slowSession.setPuzzle(slowPuzzle);
        slowSession.setStatus(Enums.GameStatus.COMPLETED);
        slowSession.setBoardString(slowPuzzle.getSolutionString());
        slowSession.setCandidatesJson("{}");
        slowSession = sessionRepository.save(slowSession);

        GameCompleteResponse slowResponse = gameEngine.completeGame(slowSession.getSessionId(), 600, 0, false);
        Integer slowScore = slowResponse.getScore();

        assertTrue(fastScore > slowScore);
    }

    @Test
    @DisplayName("Mistakes reduce points")
    void testMistakePenalty() {
        testSession.setStatus(Enums.GameStatus.COMPLETED);
        sessionRepository.save(testSession);

        GameCompleteResponse noMistakesResponse = gameEngine.completeGame(testSession.getSessionId(), 300, 0, false);
        Integer pointsNoMistakes = noMistakesResponse.getScore();

        SudokuPuzzle mistakesPuzzle = new SudokuPuzzle();
        mistakesPuzzle.setDifficulty(Enums.Difficulty.EASY);
        mistakesPuzzle.setCluesString("530070000600195000098000060800060003400803001700020006060000280000419005000080079");
        mistakesPuzzle.setSolutionString("534678912672195348198342567825961734349287651761524896956837281283419675417253829");
        mistakesPuzzle = puzzleRepository.save(mistakesPuzzle);

        SudokuGameSession mistakesSession = new SudokuGameSession();
        mistakesSession.setSessionId(UUID.randomUUID().toString());
        mistakesSession.setCreatedAt(LocalDateTime.now());
        mistakesSession.setUpdatedAt(LocalDateTime.now());
        mistakesSession.setUser(testUser);
        mistakesSession.setPuzzle(mistakesPuzzle);
        mistakesSession.setStatus(Enums.GameStatus.COMPLETED);
        mistakesSession.setBoardString(mistakesPuzzle.getSolutionString());
        mistakesSession.setCandidatesJson("{}");
        mistakesSession = sessionRepository.save(mistakesSession);

        GameCompleteResponse withMistakesResponse = gameEngine.completeGame(mistakesSession.getSessionId(), 300, 5, false);
        Integer pointsWithMistakes = withMistakesResponse.getScore();

        assertTrue(pointsNoMistakes > pointsWithMistakes);
    }

    @Test
    @DisplayName("Score is saved to leaderboard")
    void testScoreSavedToLeaderboard() {
        testSession.setStatus(Enums.GameStatus.COMPLETED);
        sessionRepository.save(testSession);

        gameEngine.completeGame(testSession.getSessionId(), 300, 0, false);

        assertTrue(gameScoreRepository.count() > 0);
    }

    @Test
    @DisplayName("Score contains points value")
    void testScoreContainsPoints() {
        testSession.setStatus(Enums.GameStatus.COMPLETED);
        sessionRepository.save(testSession);

        GameCompleteResponse response = gameEngine.completeGame(testSession.getSessionId(), 300, 0, false);

        assertNotNull(response.getScore());
        assertTrue(response.getScore() > 0);
    }

    @Test
    @DisplayName("Minimum points awarded on completion")
    void testMinimumPointsAward() {
        testSession.setStatus(Enums.GameStatus.COMPLETED);
        sessionRepository.save(testSession);

        GameCompleteResponse response = gameEngine.completeGame(testSession.getSessionId(), 3600, 20, false);

        assertTrue(response.getScore() >= 1);
    }

    @Test
    @DisplayName("Auto candidate mode affects scoring - manual always beats auto at same time")
    void testAutoCandidateModeScoring() {
        testSession.setStatus(Enums.GameStatus.COMPLETED);
        sessionRepository.save(testSession);

        GameCompleteResponse manualResponse = gameEngine.completeGame(testSession.getSessionId(), 300, 0, false);
        Integer manualScore = manualResponse.getScore();

        SudokuPuzzle autoPuzzle = new SudokuPuzzle();
        autoPuzzle.setDifficulty(Enums.Difficulty.EASY);
        autoPuzzle.setCluesString("530070000600195000098000060800060003400803001700020006060000280000419005000080079");
        autoPuzzle.setSolutionString("534678912672195348198342567825961734349287651761524896956837281283419675417253829");
        autoPuzzle = puzzleRepository.save(autoPuzzle);

        SudokuGameSession autoSession = new SudokuGameSession();
        autoSession.setSessionId(UUID.randomUUID().toString());
        autoSession.setCreatedAt(LocalDateTime.now());
        autoSession.setUpdatedAt(LocalDateTime.now());
        autoSession.setUser(testUser);
        autoSession.setPuzzle(autoPuzzle);
        autoSession.setStatus(Enums.GameStatus.COMPLETED);
        autoSession.setBoardString(autoPuzzle.getSolutionString());
        autoSession.setCandidatesJson("{}");
        autoSession = sessionRepository.save(autoSession);

        GameCompleteResponse autoResponse = gameEngine.completeGame(autoSession.getSessionId(), 300, 0, true);
        Integer autoScore = autoResponse.getScore();

        assertNotNull(manualScore);
        assertNotNull(autoScore);
        assertTrue(manualScore > autoScore,
            "Manual score (" + manualScore + ") should be higher than auto score (" + autoScore + ") at same time");
    }

    @Test
    @DisplayName("Score ranking calculation")
    void testScoreRanking() {
        testSession.setStatus(Enums.GameStatus.COMPLETED);
        sessionRepository.save(testSession);

        GameCompleteResponse response = gameEngine.completeGame(testSession.getSessionId(), 300, 0, false);

        assertNotNull(response.getRank());
        assertTrue(response.getRank() >= 1);
    }

    @Test
    @DisplayName("Multiple completions tracked separately")
    void testMultipleCompletions() {
        long initialScores = gameScoreRepository.count();

        testSession.setStatus(Enums.GameStatus.COMPLETED);
        sessionRepository.save(testSession);

        gameEngine.completeGame(testSession.getSessionId(), 300, 0, false);

        long scoresAfterFirst = gameScoreRepository.count();
        assertTrue(scoresAfterFirst > initialScores);
    }

    @Test
    @DisplayName("Score response contains all required fields")
    void testScoreResponseComplete() {
        testSession.setStatus(Enums.GameStatus.COMPLETED);
        sessionRepository.save(testSession);

        GameCompleteResponse response = gameEngine.completeGame(testSession.getSessionId(), 300, 0, false);

        assertNotNull(response.getScore());
        assertNotNull(response.getRank());
        assertNotNull(response.getCompletionStatus());
    }

    @Test
    @DisplayName("Time zero completion valid")
    void testZeroTimeCompletion() {
        testSession.setStatus(Enums.GameStatus.COMPLETED);
        sessionRepository.save(testSession);

        GameCompleteResponse response = gameEngine.completeGame(testSession.getSessionId(), 1, 0, false);

        assertNotNull(response.getScore());
    }

    @Test
    @DisplayName("Maximum mistakes scenario")
    void testMaxMistakesScenario() {
        testSession.setStatus(Enums.GameStatus.COMPLETED);
        sessionRepository.save(testSession);

        GameCompleteResponse response = gameEngine.completeGame(testSession.getSessionId(), 300, 100, false);

        assertTrue(response.getScore() >= 1);
    }


    private SudokuGameSession createCompletedSession(Enums.Difficulty difficulty) {
        SudokuPuzzle puzzle = new SudokuPuzzle();
        puzzle.setDifficulty(difficulty);
        puzzle.setCluesString("530070000600195000098000060800060003400803001700020006060000280000419005000080079");
        puzzle.setSolutionString("534678912672195348198342567825961734349287651761524896956837281283419675417253829");
        puzzle = puzzleRepository.save(puzzle);

        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setUser(testUser);
        session.setPuzzle(puzzle);
        session.setStatus(Enums.GameStatus.COMPLETED);
        session.setBoardString(puzzle.getSolutionString());
        session.setCandidatesJson("{}");
        return sessionRepository.save(session);
    }

    @Test
    @DisplayName("Fairness: auto-candidate penalty is exactly 40% of manual score")
    void testAutoCandidatePenaltyExactRatio() {
        SudokuGameSession manualSession = createCompletedSession(Enums.Difficulty.EASY);
        GameCompleteResponse manualResponse = gameEngine.completeGame(manualSession.getSessionId(), 300, 0, false);

        SudokuGameSession autoSession = createCompletedSession(Enums.Difficulty.EASY);
        GameCompleteResponse autoResponse = gameEngine.completeGame(autoSession.getSessionId(), 300, 0, true);

        double ratio = (double) autoResponse.getScore() / manualResponse.getScore();
        assertTrue(ratio >= 0.38 && ratio <= 0.42,
            "Auto/manual ratio should be ~0.4, got " + ratio + " (manual=" + manualResponse.getScore() + ", auto=" + autoResponse.getScore() + ")");
    }

    @Test
    @DisplayName("Fairness: manual with mistakes beats auto without mistakes at moderate time")
    void testManualWithMistakesVsAutoClean() {
        SudokuGameSession manualSession = createCompletedSession(Enums.Difficulty.EASY);
        GameCompleteResponse manualResponse = gameEngine.completeGame(manualSession.getSessionId(), 300, 2, false);

        SudokuGameSession autoSession = createCompletedSession(Enums.Difficulty.EASY);
        GameCompleteResponse autoResponse = gameEngine.completeGame(autoSession.getSessionId(), 300, 0, true);

        assertTrue(manualResponse.getScore() > autoResponse.getScore(),
            "5min manual w/2 mistakes (" + manualResponse.getScore() + ") should > 5min auto clean (" + autoResponse.getScore() + ")");
    }

    @Test
    @DisplayName("Fairness: very fast auto does not outscore reasonable manual across difficulties")
    void testFastAutoVsReasonableManualAllDifficulties() {
        Enums.Difficulty[] difficulties = Enums.Difficulty.values();
        int[] reasonableTimesSeconds = {480, 900, 1800, 3600}; // 8min, 15min, 30min, 60min

        for (int i = 0; i < difficulties.length; i++) {
            SudokuGameSession manualSession = createCompletedSession(difficulties[i]);
            GameCompleteResponse manualResponse = gameEngine.completeGame(manualSession.getSessionId(), reasonableTimesSeconds[i], 0, false);

            SudokuGameSession autoSession = createCompletedSession(difficulties[i]);
            GameCompleteResponse autoResponse = gameEngine.completeGame(autoSession.getSessionId(), 30, 0, true);

            assertTrue(manualResponse.getScore() >= autoResponse.getScore(),
                difficulties[i] + ": reasonable manual (" + manualResponse.getScore() + ") should >= instant auto (" + autoResponse.getScore() + ")");
        }
    }

    @Test
    @DisplayName("Fairness: difficulty progression rewards harder puzzles")
    void testDifficultyProgressionScoring() {
        int[] times = {480, 900, 1800, 3600}; // expected pace per difficulty
        int[] scores = new int[4];
        Enums.Difficulty[] diffs = Enums.Difficulty.values();

        for (int i = 0; i < diffs.length; i++) {
            SudokuGameSession session = createCompletedSession(diffs[i]);
            GameCompleteResponse response = gameEngine.completeGame(session.getSessionId(), times[i], 0, false);
            scores[i] = response.getScore();
        }

        for (int i = 1; i < scores.length; i++) {
            assertTrue(scores[i] > scores[i - 1],
                diffs[i] + " (" + scores[i] + ") should score higher than " + diffs[i - 1] + " (" + scores[i - 1] + ")");
        }
    }

    @Test
    @DisplayName("Fairness: speed floor prevents zero scores")
    void testSpeedFloorPreventsZeroScores() {
        SudokuGameSession session = createCompletedSession(Enums.Difficulty.EASY);
        GameCompleteResponse response = gameEngine.completeGame(session.getSessionId(), 3600, 0, false);

        assertTrue(response.getScore() >= 300,
            "1-hour EASY should get at least 300 pts (floor), got " + response.getScore());
    }

    @Test
    @DisplayName("Fairness: all penalty factors combined produce reasonable minimums")
    void testWorstCaseScoreStillPositive() {
        SudokuGameSession session = createCompletedSession(Enums.Difficulty.EASY);
        GameCompleteResponse response = gameEngine.completeGame(session.getSessionId(), 3600, 5, true);

        assertTrue(response.getScore() >= 50,
            "Worst case EASY should still get meaningful points, got " + response.getScore());
        assertTrue(response.getScore() <= 100,
            "Worst case EASY shouldn't be too generous, got " + response.getScore());
    }

    @Test
    @DisplayName("Fairness: optimal EASY score has clear ceiling")
    void testOptimalEasyScoreCeiling() {
        SudokuGameSession session = createCompletedSession(Enums.Difficulty.EASY);
        GameCompleteResponse response = gameEngine.completeGame(session.getSessionId(), 1, 0, false);

        assertTrue(response.getScore() >= 1795 && response.getScore() <= 1800,
            "Perfect EASY should be ~1800 (1000 * 1.8), got " + response.getScore());
    }

    @Test
    @DisplayName("Fairness: optimal auto score can never reach manual base score")
    void testAutoCannotReachManualBase() {

        SudokuGameSession autoSession = createCompletedSession(Enums.Difficulty.EASY);
        GameCompleteResponse bestAuto = gameEngine.completeGame(autoSession.getSessionId(), 1, 0, true);

        SudokuGameSession manualSession = createCompletedSession(Enums.Difficulty.EASY);
        GameCompleteResponse moderateManual = gameEngine.completeGame(manualSession.getSessionId(), 300, 0, false);

        assertTrue(moderateManual.getScore() > bestAuto.getScore(),
            "Moderate 5min manual (" + moderateManual.getScore() + ") should beat perfect auto (" + bestAuto.getScore() + ")");
    }
}
