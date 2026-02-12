package com.ltp.sudomaster.controller;

import com.ltp.sudomaster.dto.AuthResponse;
import com.ltp.sudomaster.dto.DeleteAccountRequest;
import com.ltp.sudomaster.dto.LoginRequest;
import com.ltp.sudomaster.dto.RegisterRequest;
import com.ltp.sudomaster.service.UserAuthenticationService;
import com.ltp.sudomaster.util.ErrorMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication operations")
public class AuthenticationController {

    @Autowired
    private UserAuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with bcrypt-encoded password")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authenticationService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Registration error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", ErrorMessages.REGISTRATION_FAILED));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user and returns authentication details")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authenticationService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", ErrorMessages.INVALID_USERNAME_OR_PASSWORD));
        } catch (Exception e) {
            log.error("Login error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", ErrorMessages.LOGIN_FAILED));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Ends the user session")
    public ResponseEntity<String> logout() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
            }
            authenticationService.logout(auth.getName());
            return ResponseEntity.ok("Logout successful");
        } catch (Exception e) {
            log.error("Logout error", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Logout failed");
        }
    }

    @DeleteMapping("/delete-account")
    @Operation(summary = "Delete user account", description = "Permanently deletes the authenticated user and their game sessions after password verification")
    public ResponseEntity<?> deleteAccount(@Valid @RequestBody DeleteAccountRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
            }

            String userId = auth.getName();
            authenticationService.deleteUser(userId, request.getPassword());
            
            return ResponseEntity.ok(Map.of(
                "message", "Account deleted successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("message", e.getMessage())
            );
        } catch (Exception e) {
            log.error("Account deletion error", e);
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("message", "Failed to delete account", "error", errorMsg)
            );
        }
    }

    @PostMapping("/deactivate-account")
    @Operation(summary = "Deactivate user account", description = "Temporarily deactivates the user account while preserving all data")
    public ResponseEntity<?> deactivateAccount() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
            }

            String userId = auth.getName();
            authenticationService.deactivateUser(userId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Account deactivated successfully"
            ));
        } catch (Exception e) {
            log.error("Account deactivation error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("message", "Failed to deactivate account", "error", e.getMessage())
            );
        }
    }

    @PutMapping("/preferences")
    @Operation(summary = "Update user preferences", description = "Saves user preferences JSON (color profile, font size, highlight settings)")
    public ResponseEntity<?> updatePreferences(@RequestBody Map<String, String> request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
            }

            String userId = auth.getName();
            String preferencesJson = request.get("preferencesJson");
            
            if (preferencesJson == null || preferencesJson.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Preferences JSON is required"));
            }

            String saved = authenticationService.updatePreferences(userId, preferencesJson);
            return ResponseEntity.ok(Map.of("preferencesJson", saved != null ? saved : ""));
        } catch (Exception e) {
            log.error("Preferences update error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("message", "Failed to update preferences")
            );
        }
    }

    @GetMapping("/preferences")
    @Operation(summary = "Get user preferences", description = "Retrieves user preferences JSON")
    public ResponseEntity<?> getPreferences() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
            }

            String userId = auth.getName();
            String preferencesJson = authenticationService.getPreferences(userId);
            return ResponseEntity.ok(Map.of("preferencesJson", preferencesJson != null ? preferencesJson : ""));
        } catch (Exception e) {
            log.error("Preferences retrieval error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("message", "Failed to retrieve preferences")
            );
        }
    }
}
