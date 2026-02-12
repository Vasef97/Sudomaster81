package com.ltp.sudomaster.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_scores", indexes = {
    @Index(name = "idx_user_difficulty", columnList = "user_id,difficulty"),
    @Index(name = "idx_user_difficulty_time", columnList = "user_id,difficulty,elapsed_time_seconds ASC"),
    @Index(name = "idx_user_difficulty_score", columnList = "user_id,difficulty,score DESC"),
    @Index(name = "idx_session_id", columnList = "session_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.Difficulty difficulty;

    @Column(nullable = false)
    private Integer elapsedTimeSeconds;

    @Column(nullable = false)
    @Builder.Default
    private Integer score = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer mistakes = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean autoCandidateMode = false;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
}
