package com.ltp.sudomaster.service;

import com.ltp.sudomaster.dto.AuthResponse;
import com.ltp.sudomaster.dto.LoginRequest;
import com.ltp.sudomaster.dto.RegisterRequest;
import com.ltp.sudomaster.entity.User;
import com.ltp.sudomaster.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User Preferences Tests")
@SuppressWarnings("null")
class UserPreferencesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAuthenticationService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SudokuGameSessionRepository sessionRepository;

    @Autowired
    private GameScoreRepository gameScoreRepository;

    private User testUser;
    private String testUserToken;

    @BeforeEach
    void setup() {
        gameScoreRepository.deleteAll();
        sessionRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("prefuser");
        testUser.setEmail("pref@test.com");
        testUser.setPasswordHash(passwordEncoder.encode("password123"));
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        String tokenData = testUser.getId() + ":" + System.currentTimeMillis();
        testUserToken = Base64.getEncoder().encodeToString(tokenData.getBytes());
    }


    @Test
    @DisplayName("User entity supports preferencesJson field")
    void testUserEntityPreferencesField() {
        String prefs = "{\"colorProfile\":\"blue\",\"fontSize\":\"large\"}";
        testUser.setPreferencesJson(prefs);
        User saved = userRepository.save(testUser);

        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(prefs, found.get().getPreferencesJson());
    }

    @Test
    @DisplayName("User preferences default to null")
    void testPreferencesDefaultNull() {
        User newUser = new User();
        newUser.setUsername("nullprefs");
        newUser.setEmail("nullprefs@test.com");
        newUser.setPasswordHash("hash");
        newUser.setIsActive(true);
        User saved = userRepository.save(newUser);

        assertNull(saved.getPreferencesJson());
    }

    @Test
    @DisplayName("User preferences can store full JSON")
    void testPreferencesStoreFullJson() {
        String fullPrefs = "{\"colorProfile\":\"purple\",\"fontSize\":\"large\",\"highlightConflicts\":false,\"highlightRowColumn\":true,\"highlightBox\":true,\"highlightIdenticalNumbers\":false}";
        testUser.setPreferencesJson(fullPrefs);
        User saved = userRepository.save(testUser);

        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(fullPrefs, found.get().getPreferencesJson());
    }

    @Test
    @DisplayName("User preferences can be updated")
    void testPreferencesUpdate() {
        testUser.setPreferencesJson("{\"colorProfile\":\"orange\"}");
        userRepository.save(testUser);

        testUser.setPreferencesJson("{\"colorProfile\":\"blue\"}");
        User updated = userRepository.save(testUser);

        assertEquals("{\"colorProfile\":\"blue\"}", updated.getPreferencesJson());
    }

    @Test
    @DisplayName("User preferences can be cleared to null")
    void testPreferencesClearToNull() {
        testUser.setPreferencesJson("{\"colorProfile\":\"green\"}");
        userRepository.save(testUser);

        testUser.setPreferencesJson(null);
        User updated = userRepository.save(testUser);
        assertNull(updated.getPreferencesJson());
    }


    @Test
    @DisplayName("Service updatePreferences saves and returns preferences")
    void testServiceUpdatePreferences() {
        String prefs = "{\"colorProfile\":\"pink\",\"fontSize\":\"normal\"}";
        String result = authService.updatePreferences(testUser.getId(), prefs);
        assertEquals(prefs, result);

        Optional<User> found = userRepository.findById(testUser.getId());
        assertTrue(found.isPresent());
        assertEquals(prefs, found.get().getPreferencesJson());
    }

    @Test
    @DisplayName("Service getPreferences returns stored preferences")
    void testServiceGetPreferences() {
        String prefs = "{\"colorProfile\":\"lavender\"}";
        testUser.setPreferencesJson(prefs);
        userRepository.save(testUser);

        String result = authService.getPreferences(testUser.getId());
        assertEquals(prefs, result);
    }

    @Test
    @DisplayName("Service getPreferences returns null when not set")
    void testServiceGetPreferencesNull() {
        String result = authService.getPreferences(testUser.getId());
        assertNull(result);
    }

    @Test
    @DisplayName("Login response includes preferences")
    void testLoginIncludesPreferences() {
        String prefs = "{\"colorProfile\":\"blue\",\"fontSize\":\"large\"}";
        testUser.setPreferencesJson(prefs);
        userRepository.save(testUser);

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmailOrUsername("prefuser");
        loginReq.setPassword("password123");

        AuthResponse response = authService.login(loginReq);
        assertEquals(prefs, response.getPreferencesJson());
    }

    @Test
    @DisplayName("Login response returns null preferences for new user")
    void testLoginNullPreferences() {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmailOrUsername("prefuser");
        loginReq.setPassword("password123");

        AuthResponse response = authService.login(loginReq);
        assertNull(response.getPreferencesJson());
    }

    @Test
    @DisplayName("Register response returns null preferences")
    void testRegisterNullPreferences() {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setUsername("newprefuser");
        regReq.setEmail("newpref@test.com");
        regReq.setPassword("password123");

        AuthResponse response = authService.register(regReq);
        assertNull(response.getPreferencesJson());
    }


    @Test
    @DisplayName("PUT /api/auth/preferences saves preferences when authenticated")
    void testPutPreferencesAuthenticated() throws Exception {
        String prefs = "{\"colorProfile\":\"green\",\"fontSize\":\"normal\"}";

        mockMvc.perform(put("/api/auth/preferences")
                .header("Authorization", "Bearer " + testUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"preferencesJson\":\"" + prefs.replace("\"", "\\\"") + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferencesJson").value(prefs));

        Optional<User> found = userRepository.findById(testUser.getId());
        assertTrue(found.isPresent());
        assertEquals(prefs, found.get().getPreferencesJson());
    }

    @Test
    @DisplayName("GET /api/auth/preferences returns preferences when authenticated")
    void testGetPreferencesAuthenticated() throws Exception {
        String prefs = "{\"colorProfile\":\"purple\"}";
        testUser.setPreferencesJson(prefs);
        userRepository.save(testUser);

        mockMvc.perform(get("/api/auth/preferences")
                .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferencesJson").value(prefs));
    }

    @Test
    @DisplayName("GET /api/auth/preferences returns empty string when no preferences")
    void testGetPreferencesEmpty() throws Exception {
        mockMvc.perform(get("/api/auth/preferences")
                .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferencesJson").value(""));
    }

    @Test
    @DisplayName("PUT /api/auth/preferences requires authentication")
    void testPutPreferencesRequiresAuth() throws Exception {
        mockMvc.perform(put("/api/auth/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"preferencesJson\":\"{}\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/auth/preferences requires authentication")
    void testGetPreferencesRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/auth/preferences"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/auth/preferences rejects empty body")
    void testPutPreferencesEmptyBody() throws Exception {
        mockMvc.perform(put("/api/auth/preferences")
                .header("Authorization", "Bearer " + testUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"preferencesJson\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Preferences persist across login sessions")
    void testPreferencesPersistAcrossSessions() {
        String prefs = "{\"colorProfile\":\"blue\",\"highlightConflicts\":false}";
        authService.updatePreferences(testUser.getId(), prefs);

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmailOrUsername("prefuser");
        loginReq.setPassword("password123");
        AuthResponse response = authService.login(loginReq);

        assertEquals(prefs, response.getPreferencesJson());
    }

    @Test
    @DisplayName("Preferences update overwrites previous value")
    void testPreferencesOverwrite() {
        authService.updatePreferences(testUser.getId(), "{\"colorProfile\":\"orange\"}");
        authService.updatePreferences(testUser.getId(), "{\"colorProfile\":\"blue\"}");

        String result = authService.getPreferences(testUser.getId());
        assertEquals("{\"colorProfile\":\"blue\"}", result);
    }
}
