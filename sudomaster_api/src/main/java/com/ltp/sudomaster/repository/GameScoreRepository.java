package com.ltp.sudomaster.repository;

import com.ltp.sudomaster.entity.Enums;
import com.ltp.sudomaster.entity.GameScore;
import com.ltp.sudomaster.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameScoreRepository extends JpaRepository<GameScore, String> {

    List<GameScore> findByUser(User user);

    List<GameScore> findByUserAndDifficulty(User user, Enums.Difficulty difficulty);

    Optional<GameScore> findBySessionId(String sessionId);

    boolean existsBySessionId(String sessionId);

    @Query("SELECT g FROM GameScore g LEFT JOIN FETCH g.user WHERE g.difficulty = :difficulty ORDER BY g.score DESC")
    List<GameScore> findTopScoresByDifficulty(@Param("difficulty") Enums.Difficulty difficulty, Pageable pageable);

    @Query("SELECT g FROM GameScore g LEFT JOIN FETCH g.user WHERE g.user = :user AND g.difficulty = :difficulty ORDER BY g.score DESC")
    Optional<GameScore> findBestScoreByUserAndDifficulty(@Param("user") User user, @Param("difficulty") Enums.Difficulty difficulty);

    @Query("SELECT g FROM GameScore g LEFT JOIN FETCH g.user ORDER BY g.score DESC")
    List<GameScore> findAllTimeTopScores(Pageable pageable);

    Long countByUser(User user);

    void deleteByUser(User user);

    @Modifying
    @Query("DELETE FROM GameScore g WHERE g.user.id = :userId")
    int deleteByUserId(@Param("userId") String userId);
}
