package com.ltp.sudomaster.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveGameRequest {

    private Integer elapsedTimeSeconds;
    private Integer errorCount;
    private Boolean autoCandidateModeUsed;
    private Boolean isAutoCandidateMode;
    private String candidatesJson;
    private String boardString;
    private String colorProfile;
    private String settingsJson;
}
