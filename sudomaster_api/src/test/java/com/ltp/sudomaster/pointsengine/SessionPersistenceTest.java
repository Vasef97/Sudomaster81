package com.ltp.sudomaster.pointsengine;

import com.ltp.sudomaster.dto.*;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Session Persistence Tests")
@Transactional
@SuppressWarnings("null")
class SessionPersistenceTest {

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

    private User user1;
    private User user2;

    private static final String CLUES = "530070000600195000098000060800060003400803001700020006060000280000419005000080079";
    private static final String SOLUTION = "534678912672195348198342567825961734349287651761524896956837281283419675417253829";

    @BeforeEach
    void setup() {
        gameScoreRepository.deleteAll();
        sessionRepository.deleteAll();
        userRepository.deleteAll();
        puzzleRepository.deleteAll();

        user1 = new User();
        user1.setUsername("persistuser1");
        user1.setEmail("persist1@test.com");
        user1.setPasswordHash("hashedpassword1");
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setUsername("persistuser2");
        user2.setEmail("persist2@test.com");
        user2.setPasswordHash("hashedpassword2");
        user2 = userRepository.save(user2);
    }

    private void setAuth(User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private SudokuGameSession createSessionForUser(User user, Enums.Difficulty difficulty, String boardString) {
        SudokuPuzzle puzzle = new SudokuPuzzle();
        puzzle.setDifficulty(difficulty);
        puzzle.setCluesString(CLUES);
        puzzle.setSolutionString(SOLUTION);
        puzzle = puzzleRepository.save(puzzle);

        SudokuGameSession session = SudokuGameSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .user(user)
                .puzzle(puzzle)
                .boardString(boardString != null ? boardString : CLUES)
                .candidatesJson("{}")
                .status(Enums.GameStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return sessionRepository.save(session);
    }

    private void addMovesToSession(String sessionId, int count) {
        SudokuGameSession session = sessionRepository.findBySessionId(sessionId).orElseThrow();
        session.setMoveCount(count);
        sessionRepository.save(session);
    }

    @Test
    @DisplayName("getSavedGame returns session for specific difficulty")
    void testGetSavedGameByDifficulty() {
        createSessionForUser(user1, Enums.Difficulty.EASY, null);
        createSessionForUser(user1, Enums.Difficulty.HARD, null);

        SavedGameResponse easyGame = gameEngine.getSavedGame(user1.getId(), "EASY");
        assertNotNull(easyGame);
        assertEquals("EASY", easyGame.getDifficulty());

        SavedGameResponse hardGame = gameEngine.getSavedGame(user1.getId(), "HARD");
        assertNotNull(hardGame);
        assertEquals("HARD", hardGame.getDifficulty());
    }

    @Test
    @DisplayName("getSavedGame returns null when no session for difficulty")
    void testGetSavedGameReturnsNullForMissingDifficulty() {
        createSessionForUser(user1, Enums.Difficulty.EASY, null);

        SavedGameResponse result = gameEngine.getSavedGame(user1.getId(), "MEDIUM");
        assertNull(result);
    }

    @Test
    @DisplayName("getSavedGame does not return completed sessions")
    void testGetSavedGameIgnoresCompleted() {
        SudokuGameSession session = createSessionForUser(user1, Enums.Difficulty.EASY, null);
        session.setStatus(Enums.GameStatus.COMPLETED);
        sessionRepository.save(session);

        SavedGameResponse result = gameEngine.getSavedGame(user1.getId(), "EASY");
        assertNull(result);
    }

    @Test
    @DisplayName("getSavedGame returns correct session data")
    void testGetSavedGameReturnsCorrectData() {
        SudokuGameSession session = createSessionForUser(user1, Enums.Difficulty.EASY, null);
        session.setElapsedTimeSeconds(120);
        session.setErrorCount(3);
        session.setAutoCandidateModeUsed(true);
        session.setIsAutoCandidateMode(false);
        session.setCandidatesJson("{\"0-1\":[2,3]}");
        sessionRepository.save(session);

        SavedGameResponse result = gameEngine.getSavedGame(user1.getId(), "EASY");
        assertNotNull(result);
        assertEquals(session.getSessionId(), result.getSessionId());
        assertEquals(120, result.getElapsedTimeSeconds());
        assertEquals(3, result.getErrorCount());
        assertTrue(result.getAutoCandidateModeUsed());
        assertFalse(result.getIsAutoCandidateMode());
        assertEquals("{\"0-1\":[2,3]}", result.getCandidatesJson());
        assertEquals(CLUES, result.getCluesString());
    }

    @Test
    @DisplayName("User can have up to 4 sessions (one per difficulty)")
    void testMultipleSessionsPerUser() {
        createSessionForUser(user1, Enums.Difficulty.EASY, null);
        createSessionForUser(user1, Enums.Difficulty.MEDIUM, null);
        createSessionForUser(user1, Enums.Difficulty.HARD, null);
        createSessionForUser(user1, Enums.Difficulty.INSANE, null);

        List<SudokuGameSession> allSessions = sessionRepository.findIncompleteSessionsByUserId(
                user1.getId(), Enums.GameStatus.IN_PROGRESS);
        assertEquals(4, allSessions.size());

        assertNotNull(gameEngine.getSavedGame(user1.getId(), "EASY"));
        assertNotNull(gameEngine.getSavedGame(user1.getId(), "MEDIUM"));
        assertNotNull(gameEngine.getSavedGame(user1.getId(), "HARD"));
        assertNotNull(gameEngine.getSavedGame(user1.getId(), "INSANE"));
    }

    @Test
    @DisplayName("createGame only cleans up session for same difficulty")
    void testCreateGamePerDifficultyCleanup() {
        SudokuGameSession easySession = createSessionForUser(user1, Enums.Difficulty.EASY, null);
        SudokuGameSession hardSession = createSessionForUser(user1, Enums.Difficulty.HARD, null);
        addMovesToSession(easySession.getSessionId(), 3);
        addMovesToSession(hardSession.getSessionId(), 5);

        setAuth(user1);
        CreateGameRequest request = new CreateGameRequest();
        request.setDifficulty("EASY");
        gameEngine.createGame(request);

        assertFalse(sessionRepository.findBySessionId(easySession.getSessionId()).isPresent());

        assertTrue(sessionRepository.findBySessionId(hardSession.getSessionId()).isPresent());
        assertEquals(5, sessionRepository.findBySessionId(hardSession.getSessionId()).orElseThrow().getMoveCount());
    }

    @Test
    @DisplayName("createGame preserves other difficulties' sessions")
    void testCreateGamePreservesOtherDifficulties() {
        createSessionForUser(user1, Enums.Difficulty.EASY, null);
        createSessionForUser(user1, Enums.Difficulty.MEDIUM, null);
        createSessionForUser(user1, Enums.Difficulty.HARD, null);

        setAuth(user1);
        CreateGameRequest request = new CreateGameRequest();
        request.setDifficulty("EASY");
        gameEngine.createGame(request);

        assertNotNull(gameEngine.getSavedGame(user1.getId(), "MEDIUM"));
        assertNotNull(gameEngine.getSavedGame(user1.getId(), "HARD"));

        List<SudokuGameSession> allSessions = sessionRepository.findIncompleteSessionsByUserId(
                user1.getId(), Enums.GameStatus.IN_PROGRESS);
        assertEquals(3, allSessions.size());
    }

    @Test
    @DisplayName("saveGameState persists timer and error data")
    void testSaveGameStatePersistence() {
        SudokuGameSession session = createSessionForUser(user1, Enums.Difficulty.EASY, null);
        setAuth(user1);

        SaveGameRequest saveRequest = SaveGameRequest.builder()
                .elapsedTimeSeconds(300)
                .errorCount(5)
                .autoCandidateModeUsed(true)
                .isAutoCandidateMode(true)
                .candidatesJson("{\"0-0\":[1,2,3]}")
                .build();

        gameEngine.saveGameState(session.getSessionId(), saveRequest);

        SudokuGameSession updated = sessionRepository.findBySessionId(session.getSessionId()).orElseThrow();
        assertEquals(300, updated.getElapsedTimeSeconds());
        assertEquals(5, updated.getErrorCount());
        assertTrue(updated.getAutoCandidateModeUsed());
        assertTrue(updated.getIsAutoCandidateMode());
        assertEquals("{\"0-0\":[1,2,3]}", updated.getCandidatesJson());
    }

    @Test
    @DisplayName("saveGameState skips completed sessions")
    void testSaveGameStateSkipsCompleted() {
        SudokuGameSession session = createSessionForUser(user1, Enums.Difficulty.EASY, null);
        session.setStatus(Enums.GameStatus.COMPLETED);
        sessionRepository.save(session);
        setAuth(user1);

        SaveGameRequest saveRequest = SaveGameRequest.builder()
                .elapsedTimeSeconds(999)
                .build();

        gameEngine.saveGameState(session.getSessionId(), saveRequest);

        SudokuGameSession result = sessionRepository.findBySessionId(session.getSessionId()).orElseThrow();
        assertEquals(0, result.getElapsedTimeSeconds());
    }

    @Test
    @DisplayName("abandonGame deletes session, moves, and puzzle")
    void testAbandonGameDeletesAllData() {
        SudokuGameSession session = createSessionForUser(user1, Enums.Difficulty.EASY, null);
        Long puzzleId = session.getPuzzle().getId();
        addMovesToSession(session.getSessionId(), 5);
        setAuth(user1);

        gameEngine.abandonGame(session.getSessionId());

        assertFalse(sessionRepository.findBySessionId(session.getSessionId()).isPresent());
        assertFalse(puzzleRepository.findById(puzzleId).isPresent());
    }

    @Test
    @DisplayName("abandonGame does not affect other sessions")
    void testAbandonGameIsolation() {
        SudokuGameSession easySession = createSessionForUser(user1, Enums.Difficulty.EASY, null);
        SudokuGameSession hardSession = createSessionForUser(user1, Enums.Difficulty.HARD, null);
        addMovesToSession(easySession.getSessionId(), 3);
        addMovesToSession(hardSession.getSessionId(), 5);
        setAuth(user1);

        gameEngine.abandonGame(easySession.getSessionId());

        assertFalse(sessionRepository.findBySessionId(easySession.getSessionId()).isPresent());
        assertTrue(sessionRepository.findBySessionId(hardSession.getSessionId()).isPresent());
        assertEquals(5, sessionRepository.findBySessionId(hardSession.getSessionId()).orElseThrow().getMoveCount());
    }

    @Test
    @DisplayName("abandonGame cannot abandon completed session")
    void testAbandonGameRejectsCompleted() {
        SudokuGameSession session = createSessionForUser(user1, Enums.Difficulty.EASY, null);
        session.setStatus(Enums.GameStatus.COMPLETED);
        sessionRepository.save(session);
        setAuth(user1);

        assertThrows(IllegalStateException.class, () ->
                gameEngine.abandonGame(session.getSessionId()));
    }

    @Test
    @DisplayName("User1 sessions are isolated from User2")
    void testUserDataIsolation() {
        SudokuGameSession session1 = createSessionForUser(user1, Enums.Difficulty.EASY, null);
        SudokuGameSession session2 = createSessionForUser(user2, Enums.Difficulty.EASY, null);
        addMovesToSession(session1.getSessionId(), 3);
        addMovesToSession(session2.getSessionId(), 5);

        setAuth(user1);
        gameEngine.abandonGame(session1.getSessionId());

        assertFalse(sessionRepository.findBySessionId(session1.getSessionId()).isPresent());
        assertTrue(sessionRepository.findBySessionId(session2.getSessionId()).isPresent());
        assertEquals(5, sessionRepository.findBySessionId(session2.getSessionId()).orElseThrow().getMoveCount());
    }

    @Test
    @DisplayName("User cannot abandon another user's session")
    void testCrossUserAbandonBlocked() {
        createSessionForUser(user2, Enums.Difficulty.EASY, null);
        SudokuGameSession user2Session = createSessionForUser(user2, Enums.Difficulty.EASY, null);
        setAuth(user1);

        assertThrows(IllegalStateException.class, () ->
                gameEngine.abandonGame(user2Session.getSessionId()));
    }

    @Test
    @DisplayName("User cannot save state for another user's session")
    void testCrossUserSaveBlocked() {
        SudokuGameSession session = createSessionForUser(user2, Enums.Difficulty.EASY, null);
        setAuth(user1);

        SaveGameRequest saveRequest = SaveGameRequest.builder()
                .elapsedTimeSeconds(999)
                .build();

        assertThrows(IllegalStateException.class, () ->
                gameEngine.saveGameState(session.getSessionId(), saveRequest));
    }

    @Test
    @DisplayName("createGame for user1 does not affect user2 sessions")
    void testCreateGameCrossUserIsolation() {
        SudokuGameSession user2Session = createSessionForUser(user2, Enums.Difficulty.EASY, null);
        addMovesToSession(user2Session.getSessionId(), 5);

        setAuth(user1);
        CreateGameRequest request = new CreateGameRequest();
        request.setDifficulty("EASY");
        gameEngine.createGame(request);

        assertTrue(sessionRepository.findBySessionId(user2Session.getSessionId()).isPresent());
        assertEquals(5, sessionRepository.findBySessionId(user2Session.getSessionId()).orElseThrow().getMoveCount());
    }

    @Test
    @DisplayName("getSavedGame only returns own user's session")
    void testGetSavedGameUserIsolation() {
        createSessionForUser(user1, Enums.Difficulty.EASY, null);
        createSessionForUser(user2, Enums.Difficulty.EASY, null);

        SavedGameResponse user1Game = gameEngine.getSavedGame(user1.getId(), "EASY");
        SavedGameResponse user2Game = gameEngine.getSavedGame(user2.getId(), "EASY");

        assertNotNull(user1Game);
        assertNotNull(user2Game);
        assertNotEquals(user1Game.getSessionId(), user2Game.getSessionId());
    }

    @Test
    @DisplayName("getSavedGame with invalid difficulty throws exception")
    void testGetSavedGameInvalidDifficulty() {
        assertThrows(IllegalArgumentException.class, () ->
                gameEngine.getSavedGame(user1.getId(), "INVALID"));
    }

    @Test
    @DisplayName("saveGameState with partial data only updates provided fields")
    void testSaveGameStatePartialUpdate() {
        SudokuGameSession session = createSessionForUser(user1, Enums.Difficulty.EASY, null);
        session.setElapsedTimeSeconds(100);
        session.setErrorCount(2);
        sessionRepository.save(session);
        setAuth(user1);

        SaveGameRequest saveRequest = SaveGameRequest.builder()
                .elapsedTimeSeconds(200)
                .build();

        gameEngine.saveGameState(session.getSessionId(), saveRequest);

        SudokuGameSession updated = sessionRepository.findBySessionId(session.getSessionId()).orElseThrow();
        assertEquals(200, updated.getElapsedTimeSeconds());
        assertEquals(2, updated.getErrorCount());
    }

    @Test
    @DisplayName("Full flow: create, save, retrieve, abandon")
    void testFullSessionLifecycle() {
        setAuth(user1);

        CreateGameRequest createRequest = new CreateGameRequest();
        createRequest.setDifficulty("MEDIUM");
        GameResponse created = gameEngine.createGame(createRequest);
        assertNotNull(created);
        String sessionId = created.getSessionId();

        SaveGameRequest saveRequest = SaveGameRequest.builder()
                .elapsedTimeSeconds(60)
                .errorCount(1)
                .autoCandidateModeUsed(false)
                .isAutoCandidateMode(false)
                .candidatesJson("{\"0-0\":[1]}")
                .build();
        gameEngine.saveGameState(sessionId, saveRequest);

        SavedGameResponse saved = gameEngine.getSavedGame(user1.getId(), "MEDIUM");
        assertNotNull(saved);
        assertEquals(sessionId, saved.getSessionId());
        assertEquals(60, saved.getElapsedTimeSeconds());
        assertEquals("MEDIUM", saved.getDifficulty());

        gameEngine.abandonGame(sessionId);

        SavedGameResponse afterAbandon = gameEngine.getSavedGame(user1.getId(), "MEDIUM");
        assertNull(afterAbandon);
    }

    @Test
    @DisplayName("New session fields have correct defaults")
    void testNewSessionFieldDefaults() {
        SudokuGameSession session = createSessionForUser(user1, Enums.Difficulty.EASY, null);

        assertEquals(0, session.getElapsedTimeSeconds());
        assertEquals(0, session.getErrorCount());
        assertFalse(session.getAutoCandidateModeUsed());
        assertFalse(session.getIsAutoCandidateMode());
    }

    @Test
    @DisplayName("saveGameState persists boardString")
    void testSaveGameStatePersistsBoardString() {
        SudokuGameSession session = createSessionForUser(user1, Enums.Difficulty.EASY, null);
        setAuth(user1);

        String newBoard = "530070000600195000098000060800060003400803001700020006060000280000419005000080079";

        SaveGameRequest saveRequest = SaveGameRequest.builder()
                .boardString(newBoard)
                .build();

        gameEngine.saveGameState(session.getSessionId(), saveRequest);

        SudokuGameSession updated = sessionRepository.findBySessionId(session.getSessionId()).orElseThrow();
        assertEquals(newBoard, updated.getBoardString());
    }

    @Test
    @DisplayName("saveGameState persists settingsJson")
    void testSaveGameStatePersistsSettingsJson() {
        SudokuGameSession session = createSessionForUser(user1, Enums.Difficulty.EASY, null);
        setAuth(user1);

        String settingsJson = "{\"errorIndicator\":true,\"threeMistakeLimit\":true,\"highlightConflicts\":true}";

        SaveGameRequest saveRequest = SaveGameRequest.builder()
                .settingsJson(settingsJson)
                .build();

        gameEngine.saveGameState(session.getSessionId(), saveRequest);

        SudokuGameSession updated = sessionRepository.findBySessionId(session.getSessionId()).orElseThrow();
        assertEquals(settingsJson, updated.getSettingsJson());
    }

    @Test
    @DisplayName("getSavedGame returns settingsJson")
    void testGetSavedGameReturnsSettingsJson() {
        SudokuGameSession session = createSessionForUser(user1, Enums.Difficulty.EASY, null);
        setAuth(user1);

        String settingsJson = "{\"errorIndicator\":false,\"threeMistakeLimit\":true}";
        session.setSettingsJson(settingsJson);
        sessionRepository.save(session);

        SavedGameResponse response = gameEngine.getSavedGame(user1.getId(), "EASY");

        assertNotNull(response);
        assertEquals(settingsJson, response.getSettingsJson());
    }

    @Test
    @DisplayName("saveGameState with null settingsJson does not overwrite existing")
    void testSaveGameStateNullSettingsJsonPreservesExisting() {
        SudokuGameSession session = createSessionForUser(user1, Enums.Difficulty.EASY, null);
        setAuth(user1);

        String existingSettings = "{\"errorIndicator\":true}";
        session.setSettingsJson(existingSettings);
        sessionRepository.save(session);

        SaveGameRequest saveRequest = SaveGameRequest.builder()
                .elapsedTimeSeconds(120)
                .build();

        gameEngine.saveGameState(session.getSessionId(), saveRequest);

        SudokuGameSession updated = sessionRepository.findBySessionId(session.getSessionId()).orElseThrow();
        assertEquals(existingSettings, updated.getSettingsJson());
    }
}
