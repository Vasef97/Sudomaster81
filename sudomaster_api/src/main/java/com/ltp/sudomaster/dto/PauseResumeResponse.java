package com.ltp.sudomaster.dto;

import java.time.LocalDateTime;

public class PauseResumeResponse {

    private String sessionId;
    private String action;
    private String message;
    private Long totalPausedSeconds;
    private String status;
    private LocalDateTime timestamp;

    public PauseResumeResponse() {}

    public PauseResumeResponse(String sessionId, String action, String message, 
                               Long totalPausedSeconds, String status, LocalDateTime timestamp) {
        this.sessionId = sessionId;
        this.action = action;
        this.message = message;
        this.totalPausedSeconds = totalPausedSeconds;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTotalPausedSeconds() {
        return totalPausedSeconds;
    }

    public void setTotalPausedSeconds(Long totalPausedSeconds) {
        this.totalPausedSeconds = totalPausedSeconds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
