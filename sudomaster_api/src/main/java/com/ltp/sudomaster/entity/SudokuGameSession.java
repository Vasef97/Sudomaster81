package com.ltp.sudomaster.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sudoku_game_session", indexes = {
    @Index(name = "idx_user_created", columnList = "user_id,created_at DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SudokuGameSession {

    @Id
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "puzzle_id", nullable = false)
    private SudokuPuzzle puzzle;

    @Column(length = 81, nullable = false)
    private String boardString;

    @Column(columnDefinition = "TEXT")
    private String candidatesJson;

    @Column(nullable = false)
    @Builder.Default
    private Integer elapsedTimeSeconds = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer errorCount = 0;

    @Builder.Default
    @Column(name = "move_count")
    private Integer moveCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean autoCandidateModeUsed = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAutoCandidateMode = false;

    @Column(length = 20)
    @Builder.Default
    private String colorProfile = "orange";

    @Column(columnDefinition = "TEXT")
    private String settingsJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.GameStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
