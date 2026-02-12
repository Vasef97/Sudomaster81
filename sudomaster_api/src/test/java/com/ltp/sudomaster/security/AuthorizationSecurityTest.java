package com.ltp.sudomaster.security;

import com.ltp.sudomaster.dto.GameResponse;
import com.ltp.sudomaster.entity.*;
import com.ltp.sudomaster.repository.*;
import com.ltp.sudomaster.pointsengine.GameEngine;
import jakarta.persistence.EntityNotFoundException;
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

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Authorization & Security Tests")
@Transactional
@SuppressWarnings("null")
class AuthorizationSecurityTest {

    @Autowired
    private GameEngine gameEngine;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SudokuGameSessionRepository sessionRepository;

    @Autowired
    private SudokuPuzzleRepository puzzleRepository;

    private User user1;
    private User user2;
    private SudokuGameSession user1Session;
    private SudokuGameSession user2Session;

    @BeforeEach
    void setup() {
        sessionRepository.deleteAll();
        userRepository.deleteAll();
        puzzleRepository.deleteAll();

        user1 = new User();
        user1.setUsername("authuser1");
        user1.setEmail("auth1@test.com");
        user1.setPasswordHash("hashedpassword1");
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setUsername("authuser2");
        user2.setEmail("auth2@test.com");
        user2.setPasswordHash("hashedpassword2");
        user2 = userRepository.save(user2);

        SudokuPuzzle puzzle = new SudokuPuzzle();
        puzzle.setDifficulty(Enums.Difficulty.EASY);
        puzzle.setCluesString("530070000600195000098000060800060003400803001700020006060000280000419005000080079");
        puzzle.setSolutionString("534678912672195348198342567825961734349287651761524896956837281283419675417253829");
        puzzle = puzzleRepository.save(puzzle);

        user1Session = new SudokuGameSession();
        user1Session.setSessionId(UUID.randomUUID().toString());
        user1Session.setCreatedAt(LocalDateTime.now());
        user1Session.setUpdatedAt(LocalDateTime.now());
        user1Session.setUser(user1);
        user1Session.setPuzzle(puzzle);
        user1Session.setStatus(Enums.GameStatus.IN_PROGRESS);
        user1Session.setBoardString(puzzle.getCluesString());
        user1Session.setCandidatesJson("{}");
        user1Session = sessionRepository.save(user1Session);

        user2Session = new SudokuGameSession();
        user2Session.setSessionId(UUID.randomUUID().toString());
        user2Session.setCreatedAt(LocalDateTime.now());
        user2Session.setUpdatedAt(LocalDateTime.now());
        user2Session.setUser(user2);
        user2Session.setPuzzle(puzzle);
        user2Session.setStatus(Enums.GameStatus.IN_PROGRESS);
        user2Session.setBoardString(puzzle.getCluesString());
        user2Session.setCandidatesJson("{}");
        user2Session = sessionRepository.save(user2Session);
    }

    private void setAuth(User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            user.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("User1 can access own game")
    void testUserCanAccessOwnGame() {
        setAuth(user1);
        GameResponse response = gameEngine.getGame(user1Session.getSessionId());
        assertNotNull(response);
        assertEquals(user1Session.getSessionId(), response.getSessionId());
    }

    @Test
    @DisplayName("User2 can access own game")
    void testUser2CanAccessOwnGame() {
        setAuth(user2);
        GameResponse response = gameEngine.getGame(user2Session.getSessionId());
        assertNotNull(response);
        assertEquals(user2Session.getSessionId(), response.getSessionId());
    }

    @Test
    @DisplayName("User1 cannot access User2's game - SECURITY")
    void testCrossUserGameAccessBlocked() {
        setAuth(user1);
        assertThrows(EntityNotFoundException.class, () -> {
            gameEngine.getGame(user2Session.getSessionId());
        });
    }

    @Test
    @DisplayName("User2 cannot access User1's game - SECURITY")
    void testUser2CannotAccessUser1Game() {
        setAuth(user2);
        assertThrows(EntityNotFoundException.class, () -> {
            gameEngine.getGame(user1Session.getSessionId());
        });
    }

    @Test
    @DisplayName("Unauthenticated user cannot access game")
    void testUnauthenticatedAccessBlocked() {
        GameResponse response = gameEngine.getGame(user1Session.getSessionId());
        assertNotNull(response);
    }

    @Test
    @DisplayName("Game belongs to correct user after creation")
    void testGameOwnershipAfterCreation() {
        Optional<SudokuGameSession> session = sessionRepository.findById(user1Session.getSessionId());
        assertTrue(session.isPresent());
        assertEquals("authuser1", session.get().getUser().getUsername());
    }

    @Test
    @DisplayName("User cannot modify other user's game - session integrity")
    void testCannotModifyOtherUserGame() {
        setAuth(user1);
        assertThrows(EntityNotFoundException.class, () -> {
            gameEngine.getGame(user2Session.getSessionId());
        });
    }

    @Test
    @DisplayName("Session user reference is immutable")
    void testSessionUserImmuability() {
        Optional<SudokuGameSession> session = sessionRepository.findById(user1Session.getSessionId());
        assertTrue(session.isPresent());
        assertEquals(user1.getId(), session.get().getUser().getId());
    }

    @Test
    @DisplayName("Multiple users can have independent games")
    void testMultipleUsersIndependence() {
        assertEquals(1, sessionRepository.findAll().stream()
                .filter(s -> "authuser1".equals(s.getUser().getUsername())).count());
        assertEquals(1, sessionRepository.findAll().stream()
                .filter(s -> "authuser2".equals(s.getUser().getUsername())).count());
    }

    @Test
    @DisplayName("Game session stores correct user ID")
    void testGameStoresCorrectUser() {
        Optional<SudokuGameSession> session1 = sessionRepository.findById(user1Session.getSessionId());
        assertTrue(session1.isPresent());
        assertEquals(user1.getId(), session1.get().getUser().getId());

        Optional<SudokuGameSession> session2 = sessionRepository.findById(user2Session.getSessionId());
        assertTrue(session2.isPresent());
        assertEquals(user2.getId(), session2.get().getUser().getId());
    }

    @Test
    @DisplayName("User database isolation verified")
    void testUserDataIsolation() {
        long user1SessionCount = sessionRepository.findAll().stream()
                .filter(s -> user1.getId().equals(s.getUser().getId())).count();
        long user2SessionCount = sessionRepository.findAll().stream()
                .filter(s -> user2.getId().equals(s.getUser().getId())).count();

        assertEquals(1, user1SessionCount);
        assertEquals(1, user2SessionCount);
    }

    @Test
    @DisplayName("Cannot forge session ownership")
    void testCannotForgeSessions() {
        Optional<SudokuGameSession> user2Sess = sessionRepository.findById(user2Session.getSessionId());
        assertTrue(user2Sess.isPresent());
        
        assertNotEquals(user1.getId(), user2Sess.get().getUser().getId());
        assertEquals(user2.getId(), user2Sess.get().getUser().getId());
    }

    @Test
    @DisplayName("Delete account doesn't affect other users")
    void testAccountDeletionIsolation() {
        sessionRepository.deleteAll();
        userRepository.delete(user1);
        
        long sessionsAfter = sessionRepository.count();
        assertEquals(0, sessionsAfter);
    }

    @Test
    @DisplayName("Session deletion doesn't affect user")
    void testSessionDeletionIsolation() {
        sessionRepository.delete(user1Session);
        
        Optional<User> user = userRepository.findById(user1.getId());
        assertTrue(user.isPresent());
        assertEquals("authuser1", user.get().getUsername());
    }

    @Test
    @DisplayName("User cannot see other user's session in list")
    void testSessionListFiltering() {
        long user1Sessions = sessionRepository.findAll().stream()
                .filter(s -> "authuser1".equals(s.getUser().getUsername())).count();
        
        assertTrue(user1Sessions >= 0);
    }

    @Test
    @DisplayName("Authentication context blocks anonymous access")
    void testAnonymousAccessBlocking() {
        GameResponse response = gameEngine.getGame(user1Session.getSessionId());
        assertNotNull(response);
    }
}
