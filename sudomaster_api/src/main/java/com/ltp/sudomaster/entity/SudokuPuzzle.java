package com.ltp.sudomaster.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sudoku_puzzle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SudokuPuzzle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 81, nullable = false)
    private String cluesString;

    @Column(length = 81, nullable = false)
    private String solutionString;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.Difficulty difficulty;
}
