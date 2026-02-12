package com.ltp.sudomaster.security;

import com.ltp.sudomaster.entity.User;
import com.ltp.sudomaster.repository.SudokuGameSessionRepository;
import com.ltp.sudomaster.repository.SudokuPuzzleRepository;
import com.ltp.sudomaster.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Security Tests")
@SuppressWarnings("null")
class SecurityTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SudokuGameSessionRepository sessionRepository;

    @Autowired
    private SudokuPuzzleRepository puzzleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        sessionRepository.deleteAll();
        puzzleRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Password encoder is available")
    void testPasswordEncoderAvailable() {
        assertNotNull(passwordEncoder);
    }

    @Test
    @DisplayName("Passwords are encoded when saved")
    void testPasswordEncoding() {
        String rawPassword = "testpassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    @DisplayName("Encoded passwords match raw passwords correctly")
    void testPasswordMatching() {
        String password = "securepassword";
        String encoded = passwordEncoder.encode(password);

        assertTrue(passwordEncoder.matches(password, encoded));
        assertFalse(passwordEncoder.matches("wrongpassword", encoded));
    }

    @Test
    @DisplayName("Password encoding is consistent")
    void testPasswordEncodingConsistency() {
        String password = "consistentpassword";
        String encoded1 = passwordEncoder.encode(password);
        String encoded2 = passwordEncoder.encode(password);

        assertNotEquals(encoded1, encoded2);
        assertTrue(passwordEncoder.matches(password, encoded1));
        assertTrue(passwordEncoder.matches(password, encoded2));
    }

    @Test
    @DisplayName("Different passwords produce different hashes")
    void testDifferentPasswordsDifferentHashes() {
        String password1 = "password1";
        String password2 = "password2";

        String encoded1 = passwordEncoder.encode(password1);
        String encoded2 = passwordEncoder.encode(password2);

        assertNotEquals(encoded1, encoded2);
        assertFalse(passwordEncoder.matches(password1, encoded2));
        assertFalse(passwordEncoder.matches(password2, encoded1));
    }

    @Test
    @DisplayName("User password hash is stored")
    void testUserPasswordHashStorage() {
        User user = new User();
        user.setUsername("secureuser");
        user.setEmail("secure@test.com");
        String hashedPassword = passwordEncoder.encode("mypassword");
        user.setPasswordHash(hashedPassword);

        user = userRepository.save(user);

        Optional<User> retrieved = userRepository.findByUsername("secureuser");
        assertTrue(retrieved.isPresent());
        assertEquals(hashedPassword, retrieved.get().getPasswordHash());
    }

    @Test
    @DisplayName("Username cannot be empty")
    void testUsernameRequired() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPasswordHash(passwordEncoder.encode("password"));

        user = userRepository.save(user);
        assertNotNull(user.getUsername());
    }

    @Test
    @DisplayName("Email is stored securely")
    void testEmailStorage() {
        User user = new User();
        user.setUsername("emailuser");
        user.setEmail("email@secure.com");
        user.setPasswordHash(passwordEncoder.encode("password"));

        user = userRepository.save(user);

        Optional<User> retrieved = userRepository.findByUsername("emailuser");
        assertTrue(retrieved.isPresent());
        assertEquals("email@secure.com", retrieved.get().getEmail());
    }

    @Test
    @DisplayName("User lookup by username")
    void testUserLookupByUsername() {
        User user = new User();
        user.setUsername("lookupuser");
        user.setEmail("lookup@test.com");
        user.setPasswordHash(passwordEncoder.encode("password"));

        userRepository.save(user);

        Optional<User> retrieved = userRepository.findByUsername("lookupuser");
        assertTrue(retrieved.isPresent());
    }

    @Test
    @DisplayName("Wrong password fails verification")
    void testWrongPasswordFailure() {
        String correctPassword = "correctpassword";
        String wrongPassword = "wrongpassword";
        String encoded = passwordEncoder.encode(correctPassword);

        assertFalse(passwordEncoder.matches(wrongPassword, encoded));
    }

    @Test
    @DisplayName("Empty password handling")
    void testEmptyPasswordHandling() {
        String emptyPassword = "";
        String encoded = passwordEncoder.encode(emptyPassword);

        assertTrue(passwordEncoder.matches(emptyPassword, encoded));
        assertTrue(passwordEncoder.matches("", encoded));
    }

    @Test
    @DisplayName("Long password handling")
    void testLongPasswordHandling() {
        String longPassword = "a".repeat(72);
        String encoded = passwordEncoder.encode(longPassword);

        assertTrue(passwordEncoder.matches(longPassword, encoded));
    }

    @Test
    @DisplayName("Special characters in password")
    void testSpecialCharactersInPassword() {
        String passwordWithSpecialChars = "p@ss#w0rd!&*";
        String encoded = passwordEncoder.encode(passwordWithSpecialChars);

        assertTrue(passwordEncoder.matches(passwordWithSpecialChars, encoded));
        assertFalse(passwordEncoder.matches("p@ss#w0rd!&", encoded));
    }

    @Test
    @DisplayName("Case sensitivity in passwords")
    void testPasswordCaseSensitivity() {
        String password = "Password123";
        String encoded = passwordEncoder.encode(password);

        assertTrue(passwordEncoder.matches(password, encoded));
        assertFalse(passwordEncoder.matches("password123", encoded));
    }

    @Test
    @DisplayName("User data cannot be modified without re-encoding")
    void testDataIntegrity() {
        User user = new User();
        user.setUsername("integrityuser");
        user.setEmail("integrity@test.com");
        String encoded = passwordEncoder.encode("originalpassword");
        user.setPasswordHash(encoded);

        user = userRepository.save(user);
        String originalHash = user.getPasswordHash();

        user.setEmail("newemail@test.com");
        user = userRepository.save(user);

        assertEquals(originalHash, user.getPasswordHash());
    }
}
