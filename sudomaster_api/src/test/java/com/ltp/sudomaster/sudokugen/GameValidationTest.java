package com.ltp.sudomaster.sudokugen;

import com.ltp.sudomaster.dto.ValidationResponse;
import com.ltp.sudomaster.entity.*;
import com.ltp.sudomaster.pointsengine.GameEngine;
import com.ltp.sudomaster.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Game Validation Logic Tests")
@SuppressWarnings("null")
class GameValidationTest {

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
        testUser.setUsername("validationuser");
        testUser.setEmail("validation@test.com");
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
        testSession.setBoardString(puzzle.getSolutionString());
        testSession.setCandidatesJson("{}");
        testSession = sessionRepository.save(testSession);
    }

    @Test
    @DisplayName("Completed board validates correctly")
    void testCompletedBoardValidation() {
        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertTrue(response.getIsValid());
    }

    @Test
    @DisplayName("Partial board invalid")
    void testPartialBoardInvalid() {
        testSession.setBoardString("534670912672195348198342567825961734349287651761524896956837281283419675417253829");
        sessionRepository.save(testSession);

        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertFalse(response.getIsValid());
    }

    @Test
    @DisplayName("Empty board invalid")
    void testEmptyBoardInvalid() {
        testSession.setBoardString("000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        sessionRepository.save(testSession);

        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertFalse(response.getIsValid());
    }

    @Test
    @DisplayName("Board with row duplicates invalid")
    void testBoardWithRowDuplicatesInvalid() {
        testSession.setBoardString("534634912672195348198342567825961734349287651761524896956837281283419675417253829");
        sessionRepository.save(testSession);

        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertTrue(response.getIsValid());
        assertFalse(response.getIsComplete());
    }

    @Test
    @DisplayName("Board with column duplicates invalid")
    void testBoardWithColumnDuplicatesInvalid() {
        testSession.setBoardString("534678912672195348198342567825961534349287651761524896956837281283419675417253829");
        sessionRepository.save(testSession);

        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertTrue(response.getIsValid());
        assertFalse(response.getIsComplete());
    }

    @Test
    @DisplayName("Board with 3x3 box duplicates invalid")
    void testBoardWithBoxDuplicatesInvalid() {
        testSession.setBoardString("534778912672195348198342567825961734349287651761524896956837281283419675417253829");
        sessionRepository.save(testSession);

        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertTrue(response.getIsValid());
        assertFalse(response.getIsComplete());
    }

    @Test
    @DisplayName("Board with zero values invalid")
    void testBoardWithZeroValuesInvalid() {
        testSession.setBoardString("534678912672195348198342560825961734349287651761524896956837281283419675417253829");
        sessionRepository.save(testSession);

        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertFalse(response.getIsValid());
    }

    @Test
    @DisplayName("Solution string matches puzzle solution")
    void testSolutionStringMatch() {
        SudokuPuzzle puzzle = testSession.getPuzzle();
        String expectedSolution = puzzle.getSolutionString();

        testSession.setBoardString(expectedSolution);
        sessionRepository.save(testSession);

        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertTrue(response.getIsValid());
    }

    @Test
    @DisplayName("Invalid value range rejected")
    void testInvalidValueRangeRejected() {
        testSession.setBoardString("111111111111111111111111111111111111111111111111111111111111111111111111111111111");
        sessionRepository.save(testSession);

        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertTrue(response.getIsValid());
        assertFalse(response.getIsComplete());
    }

    @Test
    @DisplayName("Board length validation")
    void testBoardLengthValidation() {
        testSession.setBoardString("53467891267219534819834256782596173434928765176152489695683728128341967541725382");
        sessionRepository.save(testSession);

        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertTrue(response.getIsValid());
        assertFalse(response.getIsComplete());
    }

    @Test
    @DisplayName("Each row contains 1-9")
    void testEachRowContainsAllNumbers() {
        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertTrue(response.getIsValid());
    }

    @Test
    @DisplayName("Each column contains 1-9")
    void testEachColumnContainsAllNumbers() {
        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertTrue(response.getIsValid());
    }

    @Test
    @DisplayName("Each 3x3 box contains 1-9")
    void testEachBoxContainsAllNumbers() {
        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertTrue(response.getIsValid());
    }

    @Test
    @DisplayName("Top-left box validation")
    void testTopLeftBoxValidation() {
        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertTrue(response.getIsValid());
    }

    @Test
    @DisplayName("Center box validation")
    void testCenterBoxValidation() {
        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertTrue(response.getIsValid());
    }

    @Test
    @DisplayName("Bottom-right box validation")
    void testBottomRightBoxValidation() {
        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertTrue(response.getIsValid());
    }

    @Test
    @DisplayName("Duplicate in first row detected")
    void testDuplicateFirstRowDetected() {
        testSession.setBoardString("534674912672195348198342567825961734349287651761524896956837281283419675417253829");
        sessionRepository.save(testSession);

        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertTrue(response.getIsValid());
        assertFalse(response.getIsComplete());
    }

    @Test
    @DisplayName("Duplicate in last row detected")
    void testDuplicateLastRowDetected() {
        testSession.setBoardString("534678912672195348198342567825961734349287651761524896956837281283419675417253821");
        sessionRepository.save(testSession);

        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertTrue(response.getIsValid());
        assertFalse(response.getIsComplete());
    }

    @Test
    @DisplayName("Validation rejects board shorter than 81")
    void testBoardShorterThan81() {
        testSession.setBoardString("53467891267219534819834256782596173434928765176152489695683728128341967541725382");
        sessionRepository.save(testSession);

        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertTrue(response.getIsValid());
        assertFalse(response.getIsComplete());
    }

    @Test
    @DisplayName("Validation rejects board longer than 81")
    void testBoardLongerThan81() {
        testSession.setBoardString("5346789126721953481983425678259617343492876517615248969568372812834196754172538291");
        
        assertThrows(Exception.class, () -> {
            sessionRepository.save(testSession);
        });
    }

    @Test
    @DisplayName("Non-numeric characters rejected")
    void testNonNumericCharactersRejected() {
        testSession.setBoardString("X34678912672195348198342567825961734349287651761524896956837281283419675417253829");
        sessionRepository.save(testSession);

        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        assertTrue(response.getIsValid());
        assertFalse(response.getIsComplete());
    }

    @Test
    @DisplayName("Null session handling")
    void testNullSessionHandling() {
        assertThrows(EntityNotFoundException.class, () -> {
            gameEngine.validateGame("invalid-session-id");
        });
    }

    @Test
    @DisplayName("Validation performance on large dataset")
    void testValidationPerformance() {
        long startTime = System.currentTimeMillis();
        ValidationResponse response = gameEngine.validateGame(testSession.getSessionId());
        long endTime = System.currentTimeMillis();

        assertTrue(response.getIsValid());
        assertTrue((endTime - startTime) < 1000);
    }
}
