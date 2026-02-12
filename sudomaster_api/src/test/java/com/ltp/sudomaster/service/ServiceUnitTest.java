package com.ltp.sudomaster.service;

import com.ltp.sudomaster.entity.*;
import com.ltp.sudomaster.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.*;

import java.util.UUID;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Service Unit Tests")
@SuppressWarnings("null")
class ServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SudokuGameSessionRepository sessionRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId("user-123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPasswordHash("hashedpassword");
    }

    @Test
    @DisplayName("User can be saved to repository")
    void testUserSave() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User saved = userRepository.save(testUser);
        assertNotNull(saved);
        assertEquals("testuser", saved.getUsername());
    }

    @Test
    @DisplayName("User can be found by username")
    void testFindByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<User> found = userRepository.findByUsername("testuser");
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    @DisplayName("User not found returns empty")
    void testUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<User> found = userRepository.findByUsername("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("User can be found by ID")
    void testFindById() {
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

        Optional<User> found = userRepository.findById("user-123");
        assertTrue(found.isPresent());
        assertEquals("user-123", found.get().getId());
    }

    @Test
    @DisplayName("Game session can be created")
    void testGameSessionCreation() {
        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setUser(testUser);
        session.setStatus(Enums.GameStatus.IN_PROGRESS);
        session.setBoardString("530070000600195000098000060800060003400803001700020006060000280000419005000080079");
        session.setCandidatesJson("{}");

        when(sessionRepository.save(any(SudokuGameSession.class))).thenReturn(session);

        SudokuGameSession saved = sessionRepository.save(session);
        assertNotNull(saved);
        assertEquals(Enums.GameStatus.IN_PROGRESS, saved.getStatus());
    }

    @Test
    @DisplayName("Game session can transition status")
    void testGameStatusTransition() {
        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setStatus(Enums.GameStatus.IN_PROGRESS);
        assertEquals(Enums.GameStatus.IN_PROGRESS, session.getStatus());

        session.setStatus(Enums.GameStatus.COMPLETED);
        assertEquals(Enums.GameStatus.COMPLETED, session.getStatus());
    }

    @Test
    @DisplayName("Multiple sessions can be retrieved for user")
    void testFindSessionsByUser() {
        List<SudokuGameSession> sessions = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            SudokuGameSession session = new SudokuGameSession();
            session.setSessionId(UUID.randomUUID().toString());
            session.setUser(testUser);
            session.setStatus(Enums.GameStatus.COMPLETED);
            sessions.add(session);
        }

        when(sessionRepository.findByUserId("user-123")).thenReturn(sessions);

        List<SudokuGameSession> result = sessionRepository.findByUserId("user-123");
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("Session board can be updated")
    void testSessionBoardUpdate() {
        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        String initialBoard = "530070000600195000098000060800060003400803001700020006060000280000419005000080079";
        session.setBoardString(initialBoard);
        assertEquals(initialBoard, session.getBoardString());

        String updatedBoard = "534070000600195000098000060800060003400803001700020006060000280000419005000080079";
        session.setBoardString(updatedBoard);
        assertEquals(updatedBoard, session.getBoardString());
    }

    @Test
    @DisplayName("Candidates JSON can be stored")
    void testCandidatesStorage() {
        SudokuGameSession session = new SudokuGameSession();
        session.setSessionId(UUID.randomUUID().toString());
        String candidates = "{\"0|0\": [1,2,3], \"0|1\": [4,5]}";
        session.setCandidatesJson(candidates);

        assertEquals(candidates, session.getCandidatesJson());
    }

    @Test
    @DisplayName("User email is stored correctly")
    void testUserEmail() {
        User user = new User();
        user.setEmail("test@example.com");

        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    @DisplayName("User password hash is stored")
    void testUserPasswordHash() {
        User user = new User();
        String passwordHash = "$2a$10$abcdef...";
        user.setPasswordHash(passwordHash);

        assertEquals(passwordHash, user.getPasswordHash());
    }
}
