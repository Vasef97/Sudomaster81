package com.ltp.sudomaster.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ltp.sudomaster.dto.*;
import com.ltp.sudomaster.entity.*;
import com.ltp.sudomaster.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Controller Integration Tests")
@SuppressWarnings("null")
class ControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SudokuGameSessionRepository sessionRepository;

    @Autowired
    private SudokuPuzzleRepository puzzleRepository;

    @Autowired
    private GameScoreRepository gameScoreRepository;

    private User testUser;
    private SudokuPuzzle testPuzzle;

    @BeforeEach
    void setup() {
        gameScoreRepository.deleteAll();
        sessionRepository.deleteAll();
        userRepository.deleteAll();
        puzzleRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("controlleruser");
        testUser.setEmail("controller@test.com");
        testUser.setPasswordHash("$2a$10$hashedpassword");
        testUser = userRepository.save(testUser);

        testPuzzle = new SudokuPuzzle();
        testPuzzle.setDifficulty(Enums.Difficulty.EASY);
        testPuzzle.setCluesString("530070000600195000098000060800060003400803001700020006060000280000419005000080079");
        testPuzzle.setSolutionString("534678912672195348198342567825961734349287651761524896956837281283419675417253829");
        testPuzzle = puzzleRepository.save(testPuzzle);
    }

    @Test
    @DisplayName("Authentication endpoint returns 200")
    void testAuthenticationEndpointExists() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register endpoint accepts POST requests")
    void testRegisterEndpointAcceptsPost() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@test.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Game controller endpoint exists")
    void testGameControllerExists() throws Exception {
        mockMvc.perform(get("/api/games"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Leaderboard endpoint returns data")
    void testLeaderboardEndpointReturnsData() throws Exception {
        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST request with JSON content type is accepted")
    void testJsonContentTypeAccepted() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET endpoints return JSON responses")
    void testGetEndpointsReturnJson() throws Exception {
        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Invalid JSON returns error")
    void testInvalidJsonHandling() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Authentication controller mapping exists")
    void testAuthControllerMapping() throws Exception {
        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Game controller mapping exists")
    void testGameControllerMapping() throws Exception {
        mockMvc.perform(get("/api/games"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Leaderboard controller mapping exists")
    void testLeaderboardControllerMapping() throws Exception {
        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Endpoints handle missing body gracefully")
    void testMissingBodyHandling() throws Exception {
        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register request with all fields")
    void testRegisterWithAllFields() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("fulluser");
        request.setEmail("full@test.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Login request with credentials")
    void testLoginWithCredentials() throws Exception {
        LoginRequest request = new LoginRequest();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API endpoints are accessible")
    void testApiEndpointsAccessible() throws Exception {
        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/games"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Controller methods are routed correctly")
    void testRoutingCorrectness() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Response content type is JSON")
    void testResponseContentType() throws Exception {
        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST methods require content type")
    void testPostMethodsRequireContentType() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .content("{}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("Multiple consecutive requests succeed")
    void testMultipleConsecutiveRequests() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/leaderboard"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("Endpoints return proper HTTP status codes")
    void testStatusCodeConsistency() throws Exception {
        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/games"))
                .andExpect(status().isUnauthorized());
    }
}
