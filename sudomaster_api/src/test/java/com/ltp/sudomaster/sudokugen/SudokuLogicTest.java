package com.ltp.sudomaster.sudokugen;

import com.ltp.sudomaster.entity.*;
import com.ltp.sudomaster.repository.*;
import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Sudoku Logic Tests")
@SuppressWarnings("null")
class SudokuLogicTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SudokuGameSessionRepository sessionRepository;

    @Autowired
    private SudokuPuzzleRepository puzzleRepository;

    private User testUser;
    private SudokuPuzzle testPuzzle;

    @BeforeEach
    void setup() {
        sessionRepository.deleteAll();
        userRepository.deleteAll();
        puzzleRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("sudokulogicuser");
        testUser.setEmail("sudokulogic@test.com");
        testUser.setPasswordHash("hashedpassword");
        testUser = userRepository.save(testUser);

        testPuzzle = new SudokuPuzzle();
        testPuzzle.setDifficulty(Enums.Difficulty.EASY);
        testPuzzle.setCluesString("530070000600195000098000060800060003400803001700020006060000280000419005000080079");
        testPuzzle.setSolutionString("534678912672195348198342567825961734349287651761524896956837281283419675417253829");
        testPuzzle = puzzleRepository.save(testPuzzle);
    }

    @Test
    @DisplayName("Board generator exists")
    void testBoardGeneratorInitialization() {
        BoardGenerator generator = new BoardGenerator();
        assertNotNull(generator);
    }

    @Test
    @DisplayName("Puzzle clues string is valid length")
    void testCluesStringLength() {
        String clues = testPuzzle.getCluesString();
        assertEquals(81, clues.length());
    }

    @Test
    @DisplayName("Puzzle solution string is valid length")
    void testSolutionStringLength() {
        String solution = testPuzzle.getSolutionString();
        assertEquals(81, solution.length());
    }

    @Test
    @DisplayName("Clues contain only digits and zeros")
    void testCluesValidFormat() {
        String clues = testPuzzle.getCluesString();
        assertTrue(clues.matches("[0-9]{81}"));
    }

    @Test
    @DisplayName("Solution contains only digits and no zeros")
    void testSolutionValidFormat() {
        String solution = testPuzzle.getSolutionString();
        assertTrue(solution.matches("[1-9]{81}"));
    }

    @Test
    @DisplayName("Clues have fewer numbers than solution")
    void testCluesHasFewNumbers() {
        String clues = testPuzzle.getCluesString();
        String solution = testPuzzle.getSolutionString();
        
        long cluesCount = clues.chars().filter(c -> c != '0').count();
        long solutionCount = solution.chars().filter(c -> c != '0').count();
        
        assertTrue(cluesCount < solutionCount);
        assertEquals(81, solutionCount);
    }

    @Test
    @DisplayName("Empty positions marked with zero in clues")
    void testEmptyPositionsInClues() {
        String clues = testPuzzle.getCluesString();
        assertTrue(clues.contains("0"));
    }

    @Test
    @DisplayName("Solution has no empty positions")
    void testNoEmptyInSolution() {
        String solution = testPuzzle.getSolutionString();
        assertFalse(solution.contains("0"));
    }

    @Test
    @DisplayName("Game session starts with clues")
    void testSessionStartsWithClues() {
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
        assertEquals(testPuzzle.getCluesString(), retrieved.get().getBoardString());
    }

    @Test
    @DisplayName("Board progress can be tracked with board string updates")
    void testBoardProgressTracking() {
        String initialBoard = testPuzzle.getCluesString();
        String progressBoard = "534070000600195000098000060800060003400803001700020006060000280000419005000080079";
        
        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setUser(testUser);
        session.setPuzzle(testPuzzle);
        session.setStatus(Enums.GameStatus.IN_PROGRESS);
        session.setBoardString(initialBoard);
        session.setCandidatesJson("{}");
        session = sessionRepository.save(session);

        session.setBoardString(progressBoard);
        session = sessionRepository.save(session);

        Optional<SudokuGameSession> retrieved = sessionRepository.findById(session.getSessionId());
        assertTrue(retrieved.isPresent());
        assertNotEquals(initialBoard, retrieved.get().getBoardString());
    }

    @Test
    @DisplayName("Difficulty levels are available")
    void testDifficultyLevels() {
        for (Enums.Difficulty difficulty : Enums.Difficulty.values()) {
            assertNotNull(difficulty);
        }
    }

    @Test
    @DisplayName("Multiple difficulty puzzles can be created")
    void testMultipleDifficultyPuzzles() {
        SudokuPuzzle mediumPuzzle = new SudokuPuzzle();
        mediumPuzzle.setDifficulty(Enums.Difficulty.MEDIUM);
        mediumPuzzle.setCluesString("530070000600195000098000060800060003400803001700020006060000280000419005000080079");
        mediumPuzzle.setSolutionString("534678912672195348198342567825961734349287651761524896956837281283419675417253829");
        mediumPuzzle = puzzleRepository.save(mediumPuzzle);

        SudokuPuzzle hardPuzzle = new SudokuPuzzle();
        hardPuzzle.setDifficulty(Enums.Difficulty.HARD);
        hardPuzzle.setCluesString("530070000600195000098000060800060003400803001700020006060000280000419005000080079");
        hardPuzzle.setSolutionString("534678912672195348198342567825961734349287651761524896956837281283419675417253829");
        hardPuzzle = puzzleRepository.save(hardPuzzle);

        assertEquals(3, puzzleRepository.count());
    }

    @Test
    @DisplayName("Candidates can be stored for move validation")
    void testCandidatesStorage() {
        String candidates = "{\"0\":[1,2],\"1\":[3,4,5]}";
        
        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setUser(testUser);
        session.setPuzzle(testPuzzle);
        session.setStatus(Enums.GameStatus.IN_PROGRESS);
        session.setBoardString(testPuzzle.getCluesString());
        session.setCandidatesJson(candidates);
        session = sessionRepository.save(session);

        Optional<SudokuGameSession> retrieved = sessionRepository.findById(session.getSessionId());
        assertTrue(retrieved.isPresent());
        assertEquals(candidates, retrieved.get().getCandidatesJson());
    }

    @Test
    @DisplayName("Puzzle validity can be tracked")
    void testPuzzleValidity() {
        assertTrue(testPuzzle.getCluesString().length() == 81);
        assertTrue(testPuzzle.getSolutionString().length() == 81);
        assertNotNull(testPuzzle.getDifficulty());
    }

    @Test
    @DisplayName("Game completion status validation")
    void testGameCompletionStatus() {
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

        Optional<SudokuGameSession> retrieved = sessionRepository.findById(session.getSessionId());
        assertTrue(retrieved.isPresent());
        assertEquals(Enums.GameStatus.COMPLETED, retrieved.get().getStatus());
    }

    @Test
    @DisplayName("Puzzle integrity maintained across saves")
    void testPuzzleIntegrity() {
        Optional<SudokuPuzzle> puzzle = puzzleRepository.findById(testPuzzle.getId());
        assertTrue(puzzle.isPresent());
        assertEquals(testPuzzle.getCluesString(), puzzle.get().getCluesString());
        assertEquals(testPuzzle.getSolutionString(), puzzle.get().getSolutionString());
    }
}
