package com.ltp.sudomaster.sudokugen;

public record GeneratedBoard(
    String puzzleBoardString,
    String solvedBoardString,
    Difficulty difficulty
) {
    public GeneratedBoard {
        if (puzzleBoardString == null || puzzleBoardString.length() != 81) {
            throw new IllegalArgumentException("puzzleBoardString must be 81 characters");
        }
        if (solvedBoardString == null || solvedBoardString.length() != 81) {
            throw new IllegalArgumentException("solvedBoardString must be 81 characters");
        }
        if (difficulty == null) {
            throw new IllegalArgumentException("difficulty must not be null");
        }
    }
}
