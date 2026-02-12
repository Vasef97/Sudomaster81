package com.ltp.sudomaster.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameCompleteResponse {

    private String sessionId;
    private String userId;
    private String difficulty;
    private Integer elapsedTime;
    private Integer score;
    private String completionStatus;
    private Integer rank;
    private String message;
}
