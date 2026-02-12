package com.ltp.sudomaster.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedGameResponse {

    private String sessionId;
    private String cluesString;
    private String boardString;
    private String candidatesJson;
    private String difficulty;
    private String status;
    private Integer elapsedTimeSeconds;
    private Integer errorCount;
    private Boolean autoCandidateModeUsed;
    private Boolean isAutoCandidateMode;
    private String colorProfile;
    private String settingsJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
