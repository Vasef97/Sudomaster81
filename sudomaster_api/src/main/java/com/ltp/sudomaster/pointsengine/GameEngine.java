package com.ltp.sudomaster.pointsengine;

import com.ltp.sudomaster.dto.*;
import com.ltp.sudomaster.entity.*;
import com.ltp.sudomaster.repository.*;
import com.ltp.sudomaster.sudokugen.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class GameEngine {

    @Autowired
    private SudokuPuzzleRepository puzzleRepository;

    @Autowired
    private SudokuGameSessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameScoreRepository gameScoreRepository;

    @Autowired
    private ScoringEngine scoringEngine;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BoardGenerator boardGenerator = new BoardGenerator();

    @Transactional
    public GameResponse createGame(CreateGameRequest request) {
        String difficultyStr = request.getDifficulty();
        Enums.Difficulty difficulty;
        
        try {
            difficulty = Enums.Difficulty.valueOf(difficultyStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid difficulty: " + difficultyStr);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalArgumentException("User not authenticated");
        }

        String userId = auth.getName();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        cleanupIncompleteSessionForDifficulty(userId, difficulty);

        com.ltp.sudomaster.sudokugen.GeneratedPuzzle generatedPuzzle;
        try {
            com.ltp.sudomaster.sudokugen.Difficulty sudokugenDifficulty = 
                com.ltp.sudomaster.sudokugen.Difficulty.valueOf(difficulty.toString());
            generatedPuzzle = boardGenerator.generate(sudokugenDifficulty);
        } catch (Exception e) {
            log.error("Failed to generate puzzle", e);
            throw new RuntimeException("Failed to generate puzzle: " + e.getMessage(), e);
        }

        SudokuPuzzle puzzle = new SudokuPuzzle();
        puzzle.setCluesString(boardToString(generatedPuzzle.puzzle()));
        puzzle.setSolutionString(boardToString(generatedPuzzle.solution()));
        puzzle.setDifficulty(difficulty);
        puzzleRepository.save(puzzle);

        String sessionId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        Map<String, List<Integer>> candidatesMap = new HashMap<>();
        for (int i = 0; i < 81; i++) {
            candidatesMap.put(String.valueOf(i), new ArrayList<>());
        }

        SudokuGameSession session = SudokuGameSession.builder()
                .sessionId(sessionId)
                .puzzle(puzzle)
                .user(user)
                .boardString(boardToString(generatedPuzzle.puzzle()))
                .candidatesJson(candidatesToJson(candidatesMap))
                .status(Enums.GameStatus.IN_PROGRESS)
                .createdAt(now)
                .updatedAt(now)
                .build();

        sessionRepository.save(session);
        return buildGameResponse(session);
    }

    @Transactional(readOnly = true)
    public GameResponse getGame(String sessionId) {
        SudokuGameSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String userId = auth.getName();
            if (session.getUser() != null && !session.getUser().getId().equals(userId)) {
                throw new EntityNotFoundException("Session not found");
            }
        }
        
        return buildGameResponse(session);
    }

    @Transactional
    public MoveResponse makeMove(String sessionId, MoveRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null && auth.isAuthenticated() ? auth.getName() : null;
        
        SudokuGameSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));
        
        if (userId != null && session.getUser() != null && !session.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Session not found");
        }

        if (session.getStatus() == Enums.GameStatus.COMPLETED) {
            return new MoveResponse(sessionId, session.getBoardString(), parseJsonToCandidates(session.getCandidatesJson()),
                    false, "Game already completed", null, session.getStatus().toString(), 
                    session.getMoveCount() != null ? session.getMoveCount() : 0);
        }

        int position = request.getPosition();
        int row = position / 9;
        int col = position % 9;
        int value = request.getValue();

        if (session.getPuzzle().getCluesString().charAt(position) != '0') {
            return new MoveResponse(sessionId, session.getBoardString(), parseJsonToCandidates(session.getCandidatesJson()),
                    false, "Cannot modify prefilled cell", null, session.getStatus().toString(), 0);
        }

        String currentBoard = session.getBoardString();

        boolean isValid = true;
        String message = "Move successful";
        if (value != 0 && hasDuplicate(currentBoard, row, col, value)) {
            isValid = false;
            message = "Invalid move: Duplicate found";
        }

        char[] chars = currentBoard.toCharArray();
        chars[position] = value == 0 ? '0' : Character.forDigit(value, 10);
        String newBoardString = new String(chars);

        session.setBoardString(newBoardString);
        session.setUpdatedAt(LocalDateTime.now());

        String solutionString = session.getPuzzle().getSolutionString();
        boolean isComplete = isPuzzleComplete(newBoardString, solutionString);
        
        if (isComplete) {
            session.setStatus(Enums.GameStatus.COMPLETED);
            message = "🎉 Congratulations! You completed the puzzle!";
        }

        session.setMoveCount((session.getMoveCount() != null ? session.getMoveCount() : 0) + 1);
        sessionRepository.save(session);

        return new MoveResponse(sessionId, newBoardString, parseJsonToCandidates(session.getCandidatesJson()),
                isValid, message, null, session.getStatus().toString(), session.getMoveCount());
    }

    @Transactional
    public ValidationResponse validateGame(String sessionId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null && auth.isAuthenticated() ? auth.getName() : null;
        
        SudokuGameSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));
        
        if (userId != null && session.getUser() != null && !session.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Session not found");
        }

        String boardString = session.getBoardString();
        String solutionString = session.getPuzzle().getSolutionString();

        List<String> errors = new ArrayList<>();
        boolean isValid = true;
        boolean isComplete = false;

        if (boardString.contains("0")) {
            errors.add("Puzzle is not complete");
            isValid = false;
        } else if (boardString.equals(solutionString)) {
            isComplete = true;
            session.setStatus(Enums.GameStatus.COMPLETED);
            sessionRepository.save(session);
        }

        String status = isComplete ? "COMPLETED" : "IN_PROGRESS";
        return new ValidationResponse(isValid, isComplete, errors, status);
    }

    @Transactional
    public GameCompleteResponse completeGame(String sessionId, Integer elapsedTime, Integer mistakes, Boolean autoCandidateMode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null && auth.isAuthenticated() ? auth.getName() : null;
        if (userId == null) {
            throw new IllegalArgumentException("User not authenticated");
        }
        
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (elapsedTime == null || elapsedTime <= 0) {
            throw new IllegalArgumentException("Elapsed time must be positive");
        }
        if (mistakes == null || mistakes < 0) {
            throw new IllegalArgumentException("Mistakes cannot be negative");
        }
        if (autoCandidateMode == null) {
            throw new IllegalArgumentException("Auto-candidate mode flag is required");
        }

        SudokuGameSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));
        
        if (session.getUser() != null && !session.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Unauthorized: You do not own this game session");
        }

        String boardString = session.getBoardString();
        String solutionString = session.getPuzzle().getSolutionString();
        
        if (boardString.contains("0")) {
            throw new IllegalStateException("Cannot complete game: Board still has empty cells");
        }
        
        if (!boardString.equals(solutionString)) {
            throw new IllegalStateException("Cannot complete game: Board solution is incorrect");
        }

        session.setStatus(Enums.GameStatus.COMPLETED);
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);

        if (gameScoreRepository.existsBySessionId(sessionId)) {
            throw new IllegalStateException("This game session has already been completed and scored");
        }

        String difficulty = session.getPuzzle().getDifficulty().toString();
        int calculatedScore = scoringEngine.calculateScore(
                Enums.Difficulty.valueOf(difficulty),
                elapsedTime,
                mistakes,
                autoCandidateMode
        );

        String message = "Game completed";
        
        Optional<GameScore> existingBestScore = findBestScore(userId, difficulty);
        
        if (existingBestScore.isPresent()) {
            GameScore existingScore = existingBestScore.get();
            int previousScore = existingScore.getScore() != null ? existingScore.getScore() : 0;
            
            if (calculatedScore > previousScore) {
                existingScore.setScore(calculatedScore);
                existingScore.setElapsedTimeSeconds(elapsedTime);
                existingScore.setMistakes(mistakes);
                existingScore.setAutoCandidateMode(autoCandidateMode);
                existingScore.setCompletedAt(LocalDateTime.now());
                
                gameScoreRepository.save(existingScore);
                message = "🏆 New personal best! Score: " + calculatedScore + " points";
            } else {
                message = "Good effort! Your best score is: " + previousScore + " points";
            }
        } else {
            GameScore newScore = GameScore.builder()
                    .sessionId(sessionId)
                    .user(session.getUser())
                    .difficulty(Enums.Difficulty.valueOf(difficulty))
                    .elapsedTimeSeconds(elapsedTime)
                    .mistakes(mistakes)
                    .autoCandidateMode(autoCandidateMode)
                    .score(calculatedScore)
                    .completedAt(LocalDateTime.now())
                    .build();
            
            gameScoreRepository.save(newScore);
            message = "🎉 Congratulations! Your score: " + calculatedScore + " points";
        }

        deleteGameSessionAndAssociatedData(session);
        List<GameScore> topScores = gameScoreRepository.findTopScoresByDifficulty(
                Enums.Difficulty.valueOf(difficulty),
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        );
        int rank = 1;
        for (GameScore score : topScores) {
            if (score.getUser().getId().equals(userId)) {
                break;
            }
            rank++;
        }

        return GameCompleteResponse.builder()
                .sessionId(sessionId)
                .userId(userId)
                .elapsedTime(elapsedTime)
                .score(calculatedScore)
                .difficulty(difficulty)
                .completionStatus("COMPLETED")
                .rank(rank)
                .message(message)
                .build();
    }

    @Transactional
    public void abandonGame(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null && auth.isAuthenticated() ? auth.getName() : null;
        
        SudokuGameSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));
        
        if (userId != null && session.getUser() != null && !session.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Unauthorized: You do not own this game session");
        }
        
        if (session.getStatus() == Enums.GameStatus.COMPLETED) {
            throw new IllegalStateException("Cannot abandon a completed game session");
        }

        deleteGameSessionAndAssociatedData(session);
    }

    @Transactional(readOnly = true)
    public SavedGameResponse getSavedGame(String userId, String difficulty) {
        Enums.Difficulty diff;
        try {
            diff = Enums.Difficulty.valueOf(difficulty.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid difficulty: " + difficulty);
        }

        List<SudokuGameSession> sessions = sessionRepository.findByUserIdAndDifficultyAndStatus(
                userId, diff, Enums.GameStatus.IN_PROGRESS);
        
        if (sessions.isEmpty()) {
            return null;
        }
        
        SudokuGameSession session = sessions.get(0);
        
        return SavedGameResponse.builder()
                .sessionId(session.getSessionId())
                .cluesString(session.getPuzzle().getCluesString())
                .boardString(session.getBoardString())
                .candidatesJson(session.getCandidatesJson())
                .difficulty(session.getPuzzle().getDifficulty().toString())
                .status(session.getStatus().toString())
                .elapsedTimeSeconds(session.getElapsedTimeSeconds())
                .errorCount(session.getErrorCount())
                .autoCandidateModeUsed(session.getAutoCandidateModeUsed())
                .isAutoCandidateMode(session.getIsAutoCandidateMode())
                .colorProfile(session.getColorProfile())
                .settingsJson(session.getSettingsJson())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    @Transactional
    public void saveGameState(String sessionId, SaveGameRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null && auth.isAuthenticated() ? auth.getName() : null;
        
        if (userId == null) {
            throw new IllegalArgumentException("User not authenticated");
        }
        
        SudokuGameSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));
        
        if (session.getUser() != null && !session.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Unauthorized: You do not own this game session");
        }
        
        if (session.getStatus() == Enums.GameStatus.COMPLETED) {
            log.debug("Skipping save for completed session: {}", sessionId);
            return;
        }
        
        if (request.getElapsedTimeSeconds() != null) {
            session.setElapsedTimeSeconds(request.getElapsedTimeSeconds());
        }
        if (request.getErrorCount() != null) {
            session.setErrorCount(request.getErrorCount());
        }
        if (request.getAutoCandidateModeUsed() != null) {
            session.setAutoCandidateModeUsed(request.getAutoCandidateModeUsed());
        }
        if (request.getIsAutoCandidateMode() != null) {
            session.setIsAutoCandidateMode(request.getIsAutoCandidateMode());
        }
        if (request.getCandidatesJson() != null) {
            session.setCandidatesJson(request.getCandidatesJson());
        }
        if (request.getBoardString() != null) {
            session.setBoardString(request.getBoardString());
        }
        if (request.getColorProfile() != null) {
            session.setColorProfile(request.getColorProfile());
        }
        if (request.getSettingsJson() != null) {
            session.setSettingsJson(request.getSettingsJson());
        }
        
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);
        log.debug("Game state saved for session: {}", sessionId);
    }

    @Transactional(readOnly = true)
    public CheckAnswerResponse checkAnswer(CheckAnswerRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (auth != null && auth.isAuthenticated()) ? auth.getName() : null;
        
        if (userId == null) {
            throw new IllegalStateException("Unauthorized: User not authenticated");
        }

        SudokuGameSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + request.getSessionId()));
        
        if (!session.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Unauthorized: This game belongs to another user");
        }

        SudokuPuzzle puzzle = session.getPuzzle();
        if (puzzle == null) {
            throw new EntityNotFoundException("Puzzle not found for session: " + request.getSessionId());
        }

        int position = request.getRow() * 9 + request.getCol();

        String cluesStr = puzzle.getCluesString();
        int clueValue = Character.getNumericValue(cluesStr.charAt(position));
        
        if (clueValue != 0) {
            throw new IllegalArgumentException("This cell is a clue and cannot be modified");
        }

        String solutionStr = puzzle.getSolutionString();
        int correctValue = Character.getNumericValue(solutionStr.charAt(position));
        
        if (correctValue == 0) {
            throw new IllegalStateException("Invalid puzzle state: solution has empty cell at (" 
                + request.getRow() + "," + request.getCol() + ")");
        }

        boolean isCorrect = request.getValue() == correctValue;
        String message = isCorrect ? "✅ Correct answer!" : "❌ Incorrect answer.";

        return CheckAnswerResponse.builder()
                .correct(isCorrect)
                .message(message)
                .build();
    }

    private boolean isPuzzleComplete(String boardString, String solutionString) {
        return !boardString.contains("0") && boardString.equals(solutionString);
    }

    private boolean hasDuplicate(String board, int row, int col, int value) {
        char valueChar = Character.forDigit(value, 10);

        for (int c = 0; c < 9; c++) {
            if (c != col && board.charAt(row * 9 + c) == valueChar) {
                return true;
            }
        }

        for (int r = 0; r < 9; r++) {
            if (r != row && board.charAt(r * 9 + col) == valueChar) {
                return true;
            }
        }

        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;
        for (int r = boxRow; r < boxRow + 3; r++) {
            for (int c = boxCol; c < boxCol + 3; c++) {
                if ((r != row || c != col) && board.charAt(r * 9 + c) == valueChar) {
                    return true;
                }
            }
        }

        return false;
    }

    private String candidatesToJson(Map<String, List<Integer>> candidates) {
        try {
            return objectMapper.writeValueAsString(candidates);
        } catch (Exception e) {
            log.warn("Error converting candidates to JSON", e);
            return "{}";
        }
    }

    private Object parseJsonToCandidates(String json) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            log.warn("Error parsing JSON to candidates", e);
            return new HashMap<>();
        }
    }

    private GameResponse buildGameResponse(SudokuGameSession session) {
        return new GameResponse(
                session.getSessionId(),
                session.getPuzzle().getId(),
                session.getPuzzle().getCluesString(),
                session.getBoardString(),
                parseJsonToCandidates(session.getCandidatesJson()),
                session.getPuzzle().getDifficulty().toString(),
                session.getStatus().toString(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }

    private Optional<GameScore> findBestScore(String userId, String difficulty) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return gameScoreRepository.findBestScoreByUserAndDifficulty(user, Enums.Difficulty.valueOf(difficulty));
    }

    private void cleanupIncompleteSessionForDifficulty(String userId, Enums.Difficulty difficulty) {
        try {
            List<SudokuGameSession> sessions = sessionRepository.findByUserIdAndDifficultyAndStatus(
                    userId, difficulty, Enums.GameStatus.IN_PROGRESS);
            
            for (SudokuGameSession session : sessions) {
                try {
                    deleteGameSessionAndAssociatedData(session);
                } catch (Exception e) {
                    log.error("Cleanup failed for session {}: {}", session.getSessionId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error during session cleanup for user {} difficulty {}: {}", userId, difficulty, e.getMessage());
        }
    }

    private boolean deleteGameSessionAndAssociatedData(SudokuGameSession session) {
        Long puzzleId = session.getPuzzle() != null ? session.getPuzzle().getId() : null;
        
        try {
            sessionRepository.delete(session);
            
            if (puzzleId != null) {
                puzzleRepository.deleteById(puzzleId);
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error during session cleanup: {}", e.getMessage());
            return false;
        }
    }

    private String boardToString(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : board) {
            for (int cell : row) {
                sb.append(cell);
            }
        }
        return sb.toString();
    }
}
