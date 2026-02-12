package com.ltp.sudomaster.dto;

public class MoveResponse {

    private String sessionId;
    private String boardString;
    private Object candidates;
    private Boolean valid;
    private String message;
    private Long moveId;
    private String completionStatus;
    private Integer moveCount;

    public MoveResponse() {}

    public MoveResponse(String sessionId, String boardString, Object candidates,
                        Boolean valid, String message, Long moveId, String completionStatus) {
        this.sessionId = sessionId;
        this.boardString = boardString;
        this.candidates = candidates;
        this.valid = valid;
        this.message = message;
        this.moveId = moveId;
        this.completionStatus = completionStatus;
    }

    public MoveResponse(String sessionId, String boardString, Object candidates,
                        Boolean valid, String message, Long moveId, String completionStatus, Integer moveCount) {
        this.sessionId = sessionId;
        this.boardString = boardString;
        this.candidates = candidates;
        this.valid = valid;
        this.message = message;
        this.moveId = moveId;
        this.completionStatus = completionStatus;
        this.moveCount = moveCount;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getBoardString() {
        return boardString;
    }

    public void setBoardString(String boardString) {
        this.boardString = boardString;
    }

    public Object getCandidates() {
        return candidates;
    }

    public void setCandidates(Object candidates) {
        this.candidates = candidates;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getMoveId() {
        return moveId;
    }

    public void setMoveId(Long moveId) {
        this.moveId = moveId;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(String completionStatus) {
        this.completionStatus = completionStatus;
    }

    public Integer getMoveCount() {
        return moveCount;
    }

    public void setMoveCount(Integer moveCount) {
        this.moveCount = moveCount;
    }
}
