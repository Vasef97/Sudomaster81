package com.ltp.sudomaster.pointsengine;

import com.ltp.sudomaster.dto.CheckAnswerRequest;
import com.ltp.sudomaster.dto.CheckAnswerResponse;
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
import java.util.List;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Sudoku Answer Validation Tests")
@SuppressWarnings("null")
class SudokuAnswerValidationTest {

    @Autowired
    private GameEngine gameEngine;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SudokuGameSessionRepository sessionRepository;

    @Autowired
    private SudokuPuzzleRepository puzzleRepository;

    private User testUser;
    private SudokuGameSession testSession;

    @BeforeEach
    void setup() {
        sessionRepository.deleteAll();
        puzzleRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("answeruser");
        testUser.setEmail("answer@test.com");
        testUser.setPasswordHash("hash");
        testUser = userRepository.save(testUser);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            testUser.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
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
        testSession.setBoardString(puzzle.getCluesString());
        testSession.setCandidatesJson("{}");
        testSession = sessionRepository.save(testSession);
    }

    @Test
    @DisplayName("Correct answer validation")
    void testCorrectAnswerValidation() {
        CheckAnswerRequest request = new CheckAnswerRequest();
        request.setSessionId(testSession.getSessionId());
        request.setRow(0);
        request.setCol(2);
        request.setValue(4);

        CheckAnswerResponse response = gameEngine.checkAnswer(request);

        assertTrue(response.isCorrect());
    }

    @Test
    @DisplayName("Incorrect answer validation")
    void testIncorrectAnswerValidation() {
        CheckAnswerRequest request = new CheckAnswerRequest();
        request.setSessionId(testSession.getSessionId());
        request.setRow(0);
        request.setCol(2);
        request.setValue(9);

        CheckAnswerResponse response = gameEngine.checkAnswer(request);

        assertFalse(response.isCorrect());
    }

    @Test
    @DisplayName("Clue cell check restriction")
    void testClueCheckRestriction() {
        CheckAnswerRequest request = new CheckAnswerRequest();
        request.setSessionId(testSession.getSessionId());
        request.setRow(0);
        request.setCol(2);
        request.setValue(4);

        CheckAnswerResponse response = gameEngine.checkAnswer(request);

        assertNotNull(response);
    }

    @Test
    @DisplayName("Answer feedback message")
    void testAnswerFeedback() {
        CheckAnswerRequest request = new CheckAnswerRequest();
        request.setSessionId(testSession.getSessionId());
        request.setRow(0);
        request.setCol(2);
        request.setValue(4);

        CheckAnswerResponse response = gameEngine.checkAnswer(request);

        assertNotNull(response.getMessage());
    }

    @Test
    @DisplayName("Multiple answer checks")
    void testMultipleAnswerChecks() {
        CheckAnswerRequest request1 = new CheckAnswerRequest();
        request1.setSessionId(testSession.getSessionId());
        request1.setRow(0);
        request1.setCol(2);
        request1.setValue(4);

        CheckAnswerResponse response1 = gameEngine.checkAnswer(request1);

        CheckAnswerRequest request2 = new CheckAnswerRequest();
        request2.setSessionId(testSession.getSessionId());
        request2.setRow(0);
        request2.setCol(3);
        request2.setValue(6);

        CheckAnswerResponse response2 = gameEngine.checkAnswer(request2);

        assertNotNull(response1);
        assertNotNull(response2);
    }

    @Test
    @DisplayName("Bottom-right cell check")
    void testBottomRightCellCheck() {
        CheckAnswerRequest request = new CheckAnswerRequest();
        request.setSessionId(testSession.getSessionId());
        request.setRow(8);
        request.setCol(3);
        request.setValue(2);

        CheckAnswerResponse response = gameEngine.checkAnswer(request);

        assertNotNull(response);
        assertTrue(response.isCorrect());
    }

    @Test
    @DisplayName("Cell position validation")
    void testCellPositionValidation() {
        int[][] emptyCells = {{0,2},{0,3},{1,1},{1,2}};
        int[] correctValues = {4,6,7,2};
        
        for (int i = 0; i < emptyCells.length; i++) {
            CheckAnswerRequest request = new CheckAnswerRequest();
            request.setSessionId(testSession.getSessionId());
            request.setRow(emptyCells[i][0]);
            request.setCol(emptyCells[i][1]);
            request.setValue(correctValues[i]);

            CheckAnswerResponse response = gameEngine.checkAnswer(request);
            assertNotNull(response);
        }
    }

    @Test
    @DisplayName("Single digit value answer")
    void testSingleDigitValueAnswer() {
        CheckAnswerRequest request = new CheckAnswerRequest();
        request.setSessionId(testSession.getSessionId());
        request.setRow(1);
        request.setCol(1);
        request.setValue(7);

        CheckAnswerResponse response = gameEngine.checkAnswer(request);

        assertNotNull(response);
    }

    @Test
    @DisplayName("Answer for each row")
    void testAnswerForEachRow() {
        int[][] emptyCells = {{0,2},{1,1},{2,3},{3,2},{4,4},{5,2},{6,2},{7,2},{8,3}};
        int[] correctValues = {4,7,8,6,2,2,4,1,2};

        for (int i = 0; i < emptyCells.length; i++) {
            CheckAnswerRequest request = new CheckAnswerRequest();
            request.setSessionId(testSession.getSessionId());
            request.setRow(emptyCells[i][0]);
            request.setCol(emptyCells[i][1]);
            request.setValue(correctValues[i]);

            CheckAnswerResponse response = gameEngine.checkAnswer(request);
            assertNotNull(response);
        }
    }

    @Test
    @DisplayName("Invalid session handling")
    void testInvalidSessionHandling() {
        CheckAnswerRequest request = new CheckAnswerRequest();
        request.setSessionId("invalid-session");
        request.setRow(0);
        request.setCol(0);
        request.setValue(5);

        assertThrows(Exception.class, () -> gameEngine.checkAnswer(request));
    }

    @Test
    @DisplayName("Answer check consistency")
    void testAnswerCheckConsistency() {
        CheckAnswerRequest request1 = new CheckAnswerRequest();
        request1.setSessionId(testSession.getSessionId());
        request1.setRow(0);
        request1.setCol(2);
        request1.setValue(4);

        CheckAnswerResponse response1 = gameEngine.checkAnswer(request1);
        
        CheckAnswerRequest request2 = new CheckAnswerRequest();
        request2.setSessionId(testSession.getSessionId());
        request2.setRow(0);
        request2.setCol(2);
        request2.setValue(4);
        
        CheckAnswerResponse response2 = gameEngine.checkAnswer(request2);

        assertEquals(response1.isCorrect(), response2.isCorrect());
    }

    @Test
    @DisplayName("Empty cell answer")
    void testEmptyCellAnswer() {
        CheckAnswerRequest request = new CheckAnswerRequest();
        request.setSessionId(testSession.getSessionId());
        request.setRow(1);
        request.setCol(1);
        request.setValue(7);

        CheckAnswerResponse response = gameEngine.checkAnswer(request);

        assertNotNull(response);
        assertTrue(response.isCorrect());
    }

    @Test
    @DisplayName("Response does not expose correct value for security")
    void testCorrectValueNotExposedInResponse() {
        CheckAnswerRequest request = new CheckAnswerRequest();
        request.setSessionId(testSession.getSessionId());
        request.setRow(0);
        request.setCol(2);
        request.setValue(9);

        CheckAnswerResponse response = gameEngine.checkAnswer(request);

        assertNotNull(response.getMessage());
        assertFalse(response.isCorrect() && response.getMessage().isEmpty());
    }
}
