package com.ltp.sudomaster.repository;

import com.ltp.sudomaster.entity.SudokuGameSession;
import com.ltp.sudomaster.entity.Enums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SudokuGameSessionRepository extends JpaRepository<SudokuGameSession, String> {

    Optional<SudokuGameSession> findBySessionId(String sessionId);

    @Query("SELECT s FROM SudokuGameSession s WHERE s.user.id = :userId AND s.status = :status ORDER BY s.updatedAt DESC")
    List<SudokuGameSession> findIncompleteSessionsByUserId(@Param("userId") String userId, @Param("status") Enums.GameStatus status);

    @Query("SELECT s FROM SudokuGameSession s WHERE s.user.id = :userId AND s.puzzle.difficulty = :difficulty AND s.status = :status ORDER BY s.updatedAt DESC")
    List<SudokuGameSession> findByUserIdAndDifficultyAndStatus(@Param("userId") String userId, @Param("difficulty") Enums.Difficulty difficulty, @Param("status") Enums.GameStatus status);

    @Query("SELECT s FROM SudokuGameSession s WHERE s.user.id = :userId")
    List<SudokuGameSession> findByUserId(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM SudokuGameSession s WHERE s.user.id = :userId")
    int deleteByUserId(@Param("userId") String userId);

    List<SudokuGameSession> findByStatusAndUpdatedAtBefore(Enums.GameStatus status, LocalDateTime cutoff);
}
