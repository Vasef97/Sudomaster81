package com.ltp.sudomaster.service;

import com.ltp.sudomaster.entity.Enums;
import com.ltp.sudomaster.entity.SudokuGameSession;
import com.ltp.sudomaster.repository.SudokuGameSessionRepository;
import com.ltp.sudomaster.repository.SudokuPuzzleRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class SessionCleanupService {

    private final SudokuGameSessionRepository sessionRepository;
    private final SudokuPuzzleRepository puzzleRepository;

    @Scheduled(initialDelay = 60000, fixedRate = 86400000)
    @Transactional
    public void cleanupStaleSessions() {
        log.info("Starting scheduled cleanup of stale game sessions...");

        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        List<SudokuGameSession> staleSessions = new java.util.ArrayList<>(sessionRepository
                .findByStatusAndUpdatedAtBefore(Enums.GameStatus.IN_PROGRESS, cutoff));
        staleSessions.addAll(sessionRepository
                .findByStatusAndUpdatedAtBefore(Enums.GameStatus.COMPLETED, cutoff));

        int cleanedUp = 0;
        for (SudokuGameSession session : staleSessions) {
            if (deleteGameSessionAndAssociatedData(session)) {
                cleanedUp++;
            }
        }

        log.info("Scheduled cleanup complete. Cleaned up {} stale sessions out of {} found.",
                cleanedUp, staleSessions.size());
    }

    private boolean deleteGameSessionAndAssociatedData(SudokuGameSession session) {
        String sessionId = session.getSessionId();
        Long puzzleId = session.getPuzzle() != null ? session.getPuzzle().getId() : null;

        try {
            sessionRepository.delete(session);

            if (puzzleId != null) {
                puzzleRepository.deleteById(puzzleId);
            }

            return true;
        } catch (Exception e) {
            log.error("Error during cleanup of session {}: {}", sessionId, e.getMessage());
            return false;
        }
    }
}
