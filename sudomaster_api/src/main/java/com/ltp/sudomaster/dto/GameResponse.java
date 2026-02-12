package com.ltp.sudomaster.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GameResponse {

    private String sessionId;
    private Long puzzleId;
    private String cluesString;
    private String boardString;
    
    @JsonProperty("puzzle")
    private Integer[] puzzle;
    
    private Object candidates;
    private String difficulty;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GameResponse() {}

    public GameResponse(String sessionId, Long puzzleId, String cluesString,
                        String boardString, Object candidates, String difficulty,
                        String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.sessionId = sessionId;
        this.puzzleId = puzzleId;
        this.cluesString = cluesString;
        this.boardString = boardString;
        this.candidates = candidates;
        this.difficulty = difficulty;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        
        this.puzzle = stringToArray(boardString);
    }

    private static Integer[] stringToArray(String boardString) {
        if (boardString == null || boardString.length() != 81) {
            return new Integer[81];
        }
        Integer[] result = new Integer[81];
        for (int i = 0; i < 81; i++) {
            result[i] = Integer.parseInt(String.valueOf(boardString.charAt(i)));
        }
        return result;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getPuzzleId() {
        return puzzleId;
    }

    public void setPuzzleId(Long puzzleId) {
        this.puzzleId = puzzleId;
    }

    public String getCluesString() {
        return cluesString;
    }

    public void setCluesString(String cluesString) {
        this.cluesString = cluesString;
    }

    public String getBoardString() {
        return boardString;
    }

    public void setBoardString(String boardString) {
        this.boardString = boardString;
        this.puzzle = stringToArray(boardString);
    }

    public Integer[] getPuzzle() {
        return puzzle;
    }

    public void setPuzzle(Integer[] puzzle) {
        this.puzzle = puzzle;
        if (puzzle != null && puzzle.length == 81) {
            StringBuilder sb = new StringBuilder();
            for (Integer val : puzzle) {
                sb.append(val);
            }
            this.boardString = sb.toString();
        }
    }

    public Object getCandidates() {
        return candidates;
    }

    public void setCandidates(Object candidates) {
        this.candidates = candidates;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
