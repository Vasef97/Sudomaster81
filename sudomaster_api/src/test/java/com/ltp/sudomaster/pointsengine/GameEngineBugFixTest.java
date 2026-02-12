package com.ltp.sudomaster.pointsengine;

import com.ltp.sudomaster.dto.MoveRequest;
import com.ltp.sudomaster.dto.MoveResponse;
import com.ltp.sudomaster.entity.*;
import com.ltp.sudomaster.repository.*;
import com.ltp.sudomaster.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Game Engine Bug Fix Tests")
@Transactional
@SuppressWarnings("null")
class GameEngineBugFixTest {

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

    private User testUser;
    private SudokuPuzzle testPuzzle;

    private static final String CLUES    = "530070000600195000098000060800060003400803001700020006060000280000419005000080079";
    private static final String SOLUTION = "534678912672195348198342567825961734349287651761524896956837281283419675417253829";

    @BeforeEach
    void setup() {
        gameScoreRepository.deleteAll();
        sessionRepository.deleteAll();
        userRepository.deleteAll();
        puzzleRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("bugfixuser");
        testUser.setEmail("bugfix@test.com");
        testUser.setPasswordHash("hashedpassword");
        testUser = userRepository.save(testUser);

        testPuzzle = new SudokuPuzzle();
        testPuzzle.setDifficulty(Enums.Difficulty.EASY);
        testPuzzle.setCluesString(CLUES);
        testPuzzle.setSolutionString(SOLUTION);
        testPuzzle = puzzleRepository.save(testPuzzle);
    }

    private void setAuth(User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private SudokuGameSession createInProgressSession(String boardString) {
        SudokuGameSession session = SudokuGameSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .user(testUser)
                .puzzle(testPuzzle)
                .boardString(boardString != null ? boardString : CLUES)
                .candidatesJson("{}")
                .status(Enums.GameStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return sessionRepository.save(session);
    }


    @Test
    @DisplayName("Move on a COMPLETED session returns error with isValid=false")
    void testMoveOnCompletedSessionBlocked() {
        setAuth(testUser);

        SudokuGameSession session = createInProgressSession(null);
        session.setStatus(Enums.GameStatus.COMPLETED);
        session = sessionRepository.save(session);

        MoveRequest request = new MoveRequest();
        request.setPosition(1);
        request.setValue(3);

        MoveResponse response = gameEngine.makeMove(session.getSessionId(), request);

        assertFalse(response.getValid(), "Move on completed session should be invalid");
        assertEquals("Game already completed", response.getMessage());
        assertEquals("COMPLETED", response.getCompletionStatus());
    }


    @Test
    @DisplayName("Future token timestamp is rejected by filter")
    void testFutureTokenTimestampRejected() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();

        long futureMs = System.currentTimeMillis() + 60_000; // 1 minute in the future
        String rawToken = testUser.getId() + ":" + futureMs;
        String encodedToken = Base64.getEncoder().encodeToString(rawToken.getBytes());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + encodedToken);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        SecurityContextHolder.clearContext();
        filter.doFilter(request, servletResponse, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "Future-timestamp token should not set authentication");
    }

    @Test
    @DisplayName("Expired token (older than 72h) is rejected by filter")
    void testExpiredTokenRejected() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();

        long expiredMs = System.currentTimeMillis() - (73 * 60 * 60 * 1000L); // 73 hours ago
        String rawToken = testUser.getId() + ":" + expiredMs;
        String encodedToken = Base64.getEncoder().encodeToString(rawToken.getBytes());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + encodedToken);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        SecurityContextHolder.clearContext();
        filter.doFilter(request, servletResponse, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "Expired token should not set authentication");
    }

    @Test
    @DisplayName("Valid token with current timestamp is accepted by filter")
    void testValidTokenAccepted() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();

        long nowMs = System.currentTimeMillis();
        String rawToken = testUser.getId() + ":" + nowMs;
        String encodedToken = Base64.getEncoder().encodeToString(rawToken.getBytes());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + encodedToken);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        SecurityContextHolder.clearContext();
        filter.doFilter(request, servletResponse, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication(),
                "Valid token should set authentication");
        assertEquals(testUser.getId(), SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }


    @Test
    @DisplayName("Move count increments correctly across multiple moves")
    void testMoveCountIncrements() {
        setAuth(testUser);
        SudokuGameSession session = createInProgressSession(null);


        MoveRequest move1 = new MoveRequest();
        move1.setPosition(2);
        move1.setValue(4);
        MoveResponse resp1 = gameEngine.makeMove(session.getSessionId(), move1);
        assertEquals(1, resp1.getMoveCount(), "After first move, moveCount should be 1");

        MoveRequest move2 = new MoveRequest();
        move2.setPosition(3);
        move2.setValue(6);
        MoveResponse resp2 = gameEngine.makeMove(session.getSessionId(), move2);
        assertEquals(2, resp2.getMoveCount(), "After second move, moveCount should be 2");

        MoveRequest move3 = new MoveRequest();
        move3.setPosition(5);
        move3.setValue(8);
        MoveResponse resp3 = gameEngine.makeMove(session.getSessionId(), move3);
        assertEquals(3, resp3.getMoveCount(), "After third move, moveCount should be 3");
    }


    @Test
    @DisplayName("Move on a prefilled (clue) cell is rejected")
    void testMoveOnPrefilledCellRejected() {
        setAuth(testUser);
        SudokuGameSession session = createInProgressSession(null);

        MoveRequest request = new MoveRequest();
        request.setPosition(0);
        request.setValue(9);

        MoveResponse response = gameEngine.makeMove(session.getSessionId(), request);

        assertFalse(response.getValid(), "Move on prefilled cell should be invalid");
        assertEquals("Cannot modify prefilled cell", response.getMessage());
    }


    @Test
    @DisplayName("Completion status becomes COMPLETED when puzzle is fully solved")
    void testCompletionStatusSetOnSolve() {
        setAuth(testUser);

        char[] almostSolved = SOLUTION.toCharArray();
        int lastEmptyPosition = -1;
        int lastEmptyValue = -1;

        for (int i = 80; i >= 0; i--) {
            if (CLUES.charAt(i) == '0') {
                lastEmptyPosition = i;
                lastEmptyValue = Character.getNumericValue(SOLUTION.charAt(i));
                almostSolved[i] = '0'; // keep this one empty
                break;
            }
        }

        assertTrue(lastEmptyPosition >= 0, "Should find at least one empty cell");
        String almostSolvedBoard = new String(almostSolved);

        SudokuGameSession session = createInProgressSession(almostSolvedBoard);

        MoveRequest request = new MoveRequest();
        request.setPosition(lastEmptyPosition);
        request.setValue(lastEmptyValue);

        MoveResponse response = gameEngine.makeMove(session.getSessionId(), request);

        assertEquals("COMPLETED", response.getCompletionStatus(), "Status should be COMPLETED after final correct move");
        assertTrue(response.getMessage().contains("Congratulations"), "Message should indicate success");
    }
}
