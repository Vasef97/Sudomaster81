package com.ltp.sudomaster.service;


import com.ltp.sudomaster.entity.*;
import com.ltp.sudomaster.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class GameScoringService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameScoreRepository gameScoreRepository;

    @SuppressWarnings("null")
    public Map<String, Object> getUserStats(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", user.getId());
        stats.put("username", user.getUsername());
        stats.put("email", user.getEmail());
        stats.put("totalGamesCompleted", gameScoreRepository.countByUser(user));

        Map<String, Object> bestScores = new HashMap<>();
        for (Enums.Difficulty difficulty : Enums.Difficulty.values()) {
            Optional<GameScore> bestScore = gameScoreRepository.findBestScoreByUserAndDifficulty(user, difficulty);
            if (bestScore.isPresent()) {
                GameScore gs = bestScore.get();
                Map<String, Object> diffStats = new HashMap<>();
                diffStats.put("score", gs.getScore());
                diffStats.put("elapsedTimeSeconds", gs.getElapsedTimeSeconds());
                diffStats.put("mistakes", gs.getMistakes());
                diffStats.put("autoCandidateMode", gs.getAutoCandidateMode());
                diffStats.put("completedAt", gs.getCompletedAt());
                bestScores.put(difficulty.toString(), diffStats);
            }
        }
        stats.put("bestScores", bestScores);

        return stats;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLeaderboard(String difficulty, int limit) {
        try {
            Enums.Difficulty diff = Enums.Difficulty.valueOf(difficulty);
            List<GameScore> topScores = gameScoreRepository.findTopScoresByDifficulty(
                    diff,
                    PageRequest.of(0, limit)
            );

            List<Map<String, Object>> leaderboard = new ArrayList<>();
            int rank = 1;
            for (GameScore score : topScores) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("rank", rank++);
                entry.put("username", score.getUser().getUsername());
                entry.put("score", score.getScore());
                entry.put("elapsedTimeSeconds", score.getElapsedTimeSeconds());
                entry.put("mistakes", score.getMistakes());
                entry.put("autoCandidateMode", score.getAutoCandidateMode());
                entry.put("difficulty", score.getDifficulty().toString());
                entry.put("completedAt", score.getCompletedAt());
                leaderboard.add(entry);
            }

            return leaderboard;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid difficulty: " + difficulty);
        }
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllTimeBestScores(int limit) {
        List<GameScore> topScores = gameScoreRepository.findAllTimeTopScores(
                PageRequest.of(0, limit)
        );

        List<Map<String, Object>> leaderboard = new ArrayList<>();
        int rank = 1;
        for (GameScore score : topScores) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("rank", rank++);
            entry.put("username", score.getUser().getUsername());
            entry.put("score", score.getScore());
            entry.put("difficulty", score.getDifficulty().toString());
            entry.put("elapsedTimeSeconds", score.getElapsedTimeSeconds());
            entry.put("mistakes", score.getMistakes());
            entry.put("autoCandidateMode", score.getAutoCandidateMode());
            entry.put("completedAt", score.getCompletedAt());
            leaderboard.add(entry);
        }

        return leaderboard;
    }

}

