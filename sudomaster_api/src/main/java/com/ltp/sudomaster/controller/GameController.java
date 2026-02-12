package com.ltp.sudomaster.controller;

import com.ltp.sudomaster.dto.*;
import com.ltp.sudomaster.pointsengine.GameEngine;
import com.ltp.sudomaster.util.ErrorMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/game")
@Tag(name = "Games", description = "Sudoku game operations")
public class GameController {

    @Autowired
    private GameEngine gameEngine;

    @PostMapping("/new")
    @Operation(summary = "Create a new Sudoku game", description = "Creates a new game session with random puzzle at specified difficulty")
    public ResponseEntity<?> createGame(@Valid @RequestBody CreateGameRequest request) {
        try {
            GameResponse response = gameEngine.createGame(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ErrorMessages.INVALID_DIFFICULTY));
        } catch (Exception e) {
            log.error("Game creation error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", ErrorMessages.FAILED_TO_GENERATE_PUZZLE));
        }
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get game state", description = "Retrieves current state of a Sudoku game")
    public ResponseEntity<GameResponse> getGame(@PathVariable String sessionId) {
        try {
            GameResponse response = gameEngine.getGame(sessionId);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("Unauthorized")) {
                log.warn("Unauthorized game access attempt: {}", sessionId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        } catch (Exception e) {
            log.error("Error retrieving game: {}", sessionId, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
    }

    @PostMapping("/{sessionId}/move")
    @Operation(summary = "Make a move", description = "Places or clears a value at specified position")
    public ResponseEntity<?> makeMove(
            @PathVariable String sessionId,
            @Valid @RequestBody MoveRequest request) {
        try {
            MoveResponse response = gameEngine.makeMove(sessionId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ErrorMessages.INVALID_MOVE));
        } catch (EntityNotFoundException e) {
            if (e.getMessage() != null && e.getMessage().contains("Session not found")) {
                log.warn("Unauthorized move attempt: {}", sessionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied"));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Game not found"));
        } catch (ObjectOptimisticLockingFailureException e) {
            log.debug("Optimistic lock conflict on move in session: {}", sessionId);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Concurrent update conflict, please retry"));
        } catch (Exception e) {
            log.error("Error making move in session: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", ErrorMessages.FAILED_TO_PROCESS_MOVE));
        }
    }

    @PostMapping("/{sessionId}/validate")
    @Operation(summary = "Validate game", description = "Checks if the current board state is valid and if puzzle is complete")
    public ResponseEntity<ValidationResponse> validateGame(@PathVariable String sessionId) {
        try {
            ValidationResponse response = gameEngine.validateGame(sessionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating game: {}", sessionId, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{sessionId}/complete")
    @Operation(summary = "Complete game", description = "Mark game as complete and save the score")
    public ResponseEntity<GameCompleteResponse> completeGame(
            @PathVariable String sessionId,
            @Valid @RequestBody GameCompleteRequest request) {
        try {
            if (sessionId == null || sessionId.trim().isEmpty()) {
                throw new IllegalArgumentException("Session ID cannot be null or empty");
            }
            if (request.getElapsedTime() == null || request.getElapsedTime() <= 0) {
                throw new IllegalArgumentException("Elapsed time must be a positive number");
            }
            if (request.getMistakes() == null || request.getMistakes() < 0) {
                throw new IllegalArgumentException("Mistakes count must be non-negative");
            }
            if (request.getAutoCandidateMode() == null) {
                throw new IllegalArgumentException("Auto-candidate mode flag is required");
            }
            
            GameCompleteResponse response = gameEngine.completeGame(
                    sessionId, 
                    request.getElapsedTime(),
                    request.getMistakes(),
                    request.getAutoCandidateMode()
            );
            
            log.info("Game completed: session={}", sessionId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Bad request in completeGame: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Invalid state in completeGame: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (ObjectOptimisticLockingFailureException e) {
            log.debug("Optimistic lock conflict on complete for session: {}", sessionId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Concurrent update conflict, please retry");
        } catch (Exception e) {
            log.error("Error completing game: session={}", sessionId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to complete game: " + e.getMessage());
        }
    }

    @GetMapping("/saved")
    @Operation(summary = "Get saved game", description = "Retrieves the user's active IN_PROGRESS game session for a specific difficulty")
    public ResponseEntity<?> getSavedGame(@RequestParam String difficulty) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
            }

            String userId = auth.getName();
            SavedGameResponse savedGame = gameEngine.getSavedGame(userId, difficulty);

            if (savedGame == null) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(savedGame);
        } catch (Exception e) {
            log.error("Error retrieving saved game", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to retrieve saved game"));
        }
    }

    @PutMapping("/{sessionId}/save")
    @Operation(summary = "Save game state", description = "Saves the current game state including timer, errors, candidates")
    public ResponseEntity<?> saveGameState(
            @PathVariable String sessionId,
            @RequestBody SaveGameRequest request) {
        try {
            gameEngine.saveGameState(sessionId, request);
            return ResponseEntity.ok(Map.of("message", "Game saved successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Game not found"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (ObjectOptimisticLockingFailureException e) {
            log.debug("Optimistic lock conflict on save for session: {}", sessionId);
            return ResponseEntity.ok(Map.of("message", "Game save skipped due to concurrent update"));
        } catch (Exception e) {
            log.error("Error saving game state: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to save game"));
        }
    }

    @DeleteMapping("/{sessionId}/abandon")
    @Operation(summary = "Abandon game", description = "Deletes an in-progress game session and all associated data")
    public ResponseEntity<?> abandonGame(@PathVariable String sessionId) {
        try {
            gameEngine.abandonGame(sessionId);
            return ResponseEntity.ok(Map.of("message", "Game abandoned successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Game not found"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error abandoning game: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to abandon game"));
        }
    }

    @PostMapping("/iscorrect")
    @Operation(summary = "Check answer", description = "Checks if the user's answer is correct by comparing with the solution")
    public ResponseEntity<?> checkAnswer(@Valid @RequestBody CheckAnswerRequest request) {
        try {
            CheckAnswerResponse response = gameEngine.checkAnswer(request);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("Unauthorized")) {
                log.warn("Unauthorized answer check attempt for session: {}", request.getSessionId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (EntityNotFoundException e) {
            log.warn("Session not found for answer check: {}", request.getSessionId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Game not found"));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid answer check request for session: {}", request.getSessionId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error checking answer for session: {}", request.getSessionId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error checking answer"));
        }
    }
}
