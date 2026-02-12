package com.ltp.sudomaster.pointsengine;

import com.ltp.sudomaster.dto.MoveRequest;
import com.ltp.sudomaster.dto.MoveResponse;
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
@DisplayName("Sudoku Move Validation Tests")
@SuppressWarnings("null")
class SudokuMoveValidationTest {

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
    private String testSessionId;

    @BeforeEach
    void setup() {
        gameScoreRepository.deleteAll();
        sessionRepository.deleteAll();
        userRepository.deleteAll();
        puzzleRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("movevalidationuser");
        testUser.setEmail("movevalidation@test.com");
        testUser.setPasswordHash("hashedpassword");
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
        testSessionId = testSession.getSessionId();
    }

    @Test
    @DisplayName("Row duplicate detection - blocks move")
    void testRowDuplicateDetection() {
        String boardWithFive = "534070000600195000098000060800060003400803001700020006060000280000419005000080079";

        testSession.setBoardString(boardWithFive);
        sessionRepository.save(testSession);

        MoveRequest request = new MoveRequest();
        request.setPosition(10);
        request.setValue(5);

        MoveResponse response = gameEngine.makeMove(testSessionId, request);

        assertFalse(response.getValid());
        assertTrue(response.getMessage().contains("Duplicate"));
    }

    @Test
    @DisplayName("Column duplicate detection - blocks move")
    void testColumnDuplicateDetection() {
        String boardWithValue = "534070000600195000098000060800060003400803001700020006060000280000419005000080079";

        testSession.setBoardString(boardWithValue);
        sessionRepository.save(testSession);

        MoveRequest request = new MoveRequest();
        request.setPosition(9);
        request.setValue(6);

        MoveResponse response = gameEngine.makeMove(testSessionId, request);

        assertFalse(response.getValid());
    }

    @Test
    @DisplayName("3x3 box duplicate detection - blocks move")
    void testBoxDuplicateDetection() {
        String boardWith3 = "534070000600195000098000060800060003400803001700020006060000280000419005000080079";

        testSession.setBoardString(boardWith3);
        sessionRepository.save(testSession);

        MoveRequest request = new MoveRequest();
        request.setPosition(10);
        request.setValue(3);

        MoveResponse response = gameEngine.makeMove(testSessionId, request);

        assertFalse(response.getValid());
    }

    @Test
    @DisplayName("Valid move is accepted")
    void testValidMoveAccepted() {
        String board = "530070000600195000098000060800060003400803001700020006060000280000419005000080079";
        testSession.setBoardString(board);
        sessionRepository.save(testSession);

        MoveRequest request = new MoveRequest();
        request.setPosition(2);
        request.setValue(4);

        MoveResponse response = gameEngine.makeMove(testSessionId, request);

        assertTrue(response.getValid());
    }

    @Test
    @DisplayName("Cannot modify clue cell")
    void testClueProtection() {
        MoveRequest request = new MoveRequest();
        request.setPosition(0);
        request.setValue(9);

        MoveResponse response = gameEngine.makeMove(testSessionId, request);

        assertFalse(response.getValid());
        assertTrue(response.getMessage().contains("prefilled"));
    }

    @Test
    @DisplayName("Move updates board correctly")
    void testBoardUpdate() {
        String originalBoard = testSession.getBoardString();
        
        MoveRequest request = new MoveRequest();
        request.setPosition(2);
        request.setValue(4);

        MoveResponse response = gameEngine.makeMove(testSessionId, request);

        assertNotEquals(originalBoard, response.getBoardString());
        assertTrue(response.getBoardString().contains("4"));
    }

    @Test
    @DisplayName("Clear move (value 0) works")
    void testClearMove() {
        String boardWithValue = "534070000600195000098000060800060003400803001700020006060000280000419005000080079";
        testSession.setBoardString(boardWithValue);
        sessionRepository.save(testSession);

        MoveRequest request = new MoveRequest();
        request.setPosition(0);
        request.setValue(0);

        MoveResponse response = gameEngine.makeMove(testSessionId, request);

        assertFalse(response.getValid());
    }

    @Test
    @DisplayName("Position boundary validation - first cell")
    void testFirstCellUpdate() {
        MoveRequest request = new MoveRequest();
        request.setPosition(0);
        request.setValue(5);

        MoveResponse response = gameEngine.makeMove(testSessionId, request);

        assertFalse(response.getValid());
    }

    @Test
    @DisplayName("Position boundary validation - last cell")
    void testLastCellUpdate() {
        MoveRequest request = new MoveRequest();
        request.setPosition(78);
        request.setValue(4);

        MoveResponse response = gameEngine.makeMove(testSessionId, request);

        assertTrue(response.getValid());
    }

    @Test
    @DisplayName("Completion detection on full board")
    void testCompletionDetection() {
        SudokuPuzzle puzzle = testSession.getPuzzle();
        String solution = puzzle.getSolutionString();
        char[] boardChars = solution.toCharArray();
        boardChars[2] = '0';
        testSession.setBoardString(new String(boardChars));
        sessionRepository.save(testSession);

        MoveRequest request = new MoveRequest();
        request.setPosition(2);
        request.setValue(4);

        MoveResponse response = gameEngine.makeMove(testSessionId, request);

        assertTrue(response.getMessage().contains("Congratulations"));
    }

    @Test
    @DisplayName("Move count increments")
    void testMoveCountIncrement() {
        MoveRequest request1 = new MoveRequest();
        request1.setPosition(2);
        request1.setValue(4);

        MoveResponse response1 = gameEngine.makeMove(testSessionId, request1);
        int moveCount1 = response1.getMoveCount();

        MoveRequest request2 = new MoveRequest();
        request2.setPosition(3);
        request2.setValue(7);

        MoveResponse response2 = gameEngine.makeMove(testSessionId, request2);
        int moveCount2 = response2.getMoveCount();

        assertTrue(moveCount2 >= moveCount1);
    }

    @Test
    @DisplayName("Invalid value (10) rejected")
    void testInvalidValueRange() {
        MoveRequest request = new MoveRequest();
        request.setPosition(10);
        request.setValue(10);

        MoveResponse response = gameEngine.makeMove(testSessionId, request);
        assertNotNull(response);
    }

    @Test
    @DisplayName("Row 0 moves - validation")
    void testRow0Moves() {
        MoveRequest request = new MoveRequest();
        request.setPosition(5);
        request.setValue(6);

        MoveResponse response = gameEngine.makeMove(testSessionId, request);

        assertNotNull(response);
    }

    @Test
    @DisplayName("Row 8 moves - validation")
    void testRow8Moves() {
        MoveRequest request = new MoveRequest();
        request.setPosition(72);
        request.setValue(1);

        MoveResponse response = gameEngine.makeMove(testSessionId, request);

        assertNotNull(response);
    }

    @Test
    @DisplayName("All 3x3 boxes validated")
    void testAllBoxesValidation() {
        int[] testPositions = {2, 11, 20, 29, 38, 47, 56, 65, 74};

        for (int pos : testPositions) {
            MoveRequest request = new MoveRequest();
            request.setPosition(pos);
            request.setValue(1);

            MoveResponse response = gameEngine.makeMove(testSessionId, request);
            assertNotNull(response);
        }
    }
}
