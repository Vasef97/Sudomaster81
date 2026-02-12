package com.ltp.sudomaster.repository;

import com.ltp.sudomaster.entity.SudokuPuzzle;
import com.ltp.sudomaster.entity.Enums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SudokuPuzzleRepository extends JpaRepository<SudokuPuzzle, Long> {

    List<SudokuPuzzle> findByDifficulty(Enums.Difficulty difficulty);

    @Query(value = "SELECT p FROM SudokuPuzzle p WHERE CAST(p.difficulty AS string) = ?1 ORDER BY FUNCTION('RAND') LIMIT 1")
    Optional<SudokuPuzzle> findRandomByDifficulty(String difficulty);
}
