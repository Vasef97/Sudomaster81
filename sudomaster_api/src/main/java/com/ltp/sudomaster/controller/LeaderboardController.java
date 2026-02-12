package com.ltp.sudomaster.controller;

import com.ltp.sudomaster.service.GameScoringService;
import com.ltp.sudomaster.util.ErrorMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "Leaderboard", description = "Leaderboard and user statistics operations")
public class LeaderboardController {

    @Autowired
    private GameScoringService scoringService;

    @GetMapping("/leaderboard")
    @Operation(summary = "Get leaderboard by difficulty", description = "Retrieves top scores for specified difficulty level")
    public ResponseEntity<?> getLeaderboard(
            @RequestParam(defaultValue = "EASY") String difficulty,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> leaderboard = scoringService.getLeaderboard(difficulty, limit);
            return ResponseEntity.ok(leaderboard);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ErrorMessages.INVALID_DIFFICULTY));
        } catch (Exception e) {
            log.error("Error retrieving leaderboard", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", ErrorMessages.FAILED_TO_GENERATE_PUZZLE));
        }
    }

    @GetMapping("/leaderboard/all-time")
    @Operation(summary = "Get all-time best scores", description = "Retrieves the best scores across all difficulty levels")
    public ResponseEntity<List<Map<String, Object>>> getAllTimeBestScores(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> leaderboard = scoringService.getAllTimeBestScores(limit);
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            log.error("Error retrieving all-time leaderboard", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get leaderboard");
        }
    }

    @GetMapping("/user/stats")
    @Operation(summary = "Get user stats", description = "Retrieves statistics and best scores for current user")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
            }
            Map<String, Object> stats = scoringService.getUserStats(auth.getName());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error retrieving user stats", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get stats");
        }
    }
}
