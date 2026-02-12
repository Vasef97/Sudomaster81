package com.ltp.sudomaster.dto;

import jakarta.validation.constraints.*;

public class CreateGameRequest {

    @NotNull(message = "Difficulty must not be null")
    @NotBlank(message = "Difficulty must not be blank")
    @Pattern(regexp = "EASY|MEDIUM|HARD|INSANE", message = "Difficulty must be EASY, MEDIUM, HARD, or INSANE")
    private String difficulty;

    public CreateGameRequest() {}

    public CreateGameRequest(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}
