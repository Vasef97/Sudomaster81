package com.ltp.sudomaster.sudokugen;

import java.util.*;

public class BoardGenerator {

    private static final int SIZE = 9;
    private static final int BLOCK_SIZE = 3;
    private static final int MAX_GENERATION_ATTEMPTS = 10000;
    private static final Random RANDOM = new Random();

    private static final int EASY_MIN_GIVENS = 36;
    private static final int EASY_MAX_GIVENS = 45;
    private static final int EASY_MIN_COST = 0;
    private static final int EASY_MAX_COST = 40;

    private static final int MEDIUM_MIN_GIVENS = 32;
    private static final int MEDIUM_MAX_GIVENS = 36;
    private static final int MEDIUM_MIN_COST = 35;
    private static final int MEDIUM_MAX_COST = 90;

    private static final int HARD_MIN_GIVENS = 26;
    private static final int HARD_MAX_GIVENS = 32;
    private static final int HARD_MIN_COST = 80;
    private static final int HARD_MAX_COST = 300;

    private static final int INSANE_MIN_GIVENS = 18;
    private static final int INSANE_MAX_GIVENS = 24;
    private static final int INSANE_MIN_COST = 200;
    private static final int INSANE_MAX_COST = Integer.MAX_VALUE;

    public GeneratedPuzzle generate(Difficulty difficulty) throws Exception {
        int[][] solution = generateCompleteBoard();
        int[][] puzzle = removeCellsUntilDifficulty(solution, difficulty);
        return new GeneratedPuzzle(puzzle, solution);
    }

    private int[][] generateCompleteBoard() {
        int[][] board = new int[SIZE][SIZE];
        fillBoardRecursively(board, 0);
        return board;
    }

    private boolean fillBoardRecursively(int[][] board, int cell) {
        if (cell == SIZE * SIZE) {
            return true;
        }

        int row = cell / SIZE;
        int col = cell % SIZE;
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= SIZE; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);

        for (int num : numbers) {
            if (isValidPlacement(board, row, col, num)) {
                board[row][col] = num;
                if (fillBoardRecursively(board, cell + 1)) {
                    return true;
                }
                board[row][col] = 0;
            }
        }

        return false;
    }

    private boolean isValidPlacement(int[][] board, int row, int col, int num) {
        for (int c = 0; c < SIZE; c++) {
            if (board[row][c] == num) {
                return false;
            }
        }

        for (int r = 0; r < SIZE; r++) {
            if (board[r][col] == num) {
                return false;
            }
        }

        int boxRow = (row / BLOCK_SIZE) * BLOCK_SIZE;
        int boxCol = (col / BLOCK_SIZE) * BLOCK_SIZE;
        for (int r = boxRow; r < boxRow + BLOCK_SIZE; r++) {
            for (int c = boxCol; c < boxCol + BLOCK_SIZE; c++) {
                if (board[r][c] == num) {
                    return false;
                }
            }
        }

        return true;
    }

    private int[][] removeCellsUntilDifficulty(int[][] board, Difficulty difficulty) throws Exception {
        DifficultyProfile profile = getDifficultyProfile(difficulty);

        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            int[][] puzzle = deepCopy(board);
            removeCellsRandomly(puzzle, profile);

            DifficultyEvaluator evaluator = new DifficultyEvaluator(puzzle);
            DifficultyEvaluator.DifficultyResult result = evaluator.evaluate();

            if (matchesProfile(result, puzzle, profile)) {
                return puzzle;
            }
        }

        throw new Exception("Failed to generate puzzle with difficulty " + difficulty + " after " + MAX_GENERATION_ATTEMPTS + " attempts");
    }

    private void removeCellsRandomly(int[][] puzzle, DifficultyProfile profile) {
        int givens = countGivens(puzzle);
        int targetGivens = RANDOM.nextInt(profile.maxGivens - profile.minGivens + 1) + profile.minGivens;

        while (givens > targetGivens) {
            int row = RANDOM.nextInt(SIZE);
            int col = RANDOM.nextInt(SIZE);

            if (puzzle[row][col] != 0) {
                puzzle[row][col] = 0;
                givens--;
            }
        }
    }

    private boolean matchesProfile(DifficultyEvaluator.DifficultyResult result, int[][] puzzle, DifficultyProfile profile) {
        int givens = countGivens(puzzle);

        if (givens < profile.minGivens || givens > profile.maxGivens) {
            return false;
        }

        if (result.cost < profile.minCost || result.cost > profile.maxCost) {
            return false;
        }

        for (Technique tech : result.techniqueUsage.keySet()) {
            if (!profile.allowedTechniques.contains(tech)) {
                return false;
            }
        }

        if (profile.forbidGuessing && result.techniqueUsage.containsKey(Technique.GUESSING)) {
            return false;
        }
        if (profile.forbidForcingChain && result.techniqueUsage.containsKey(Technique.FORCING_CHAIN)) {
            return false;
        }
        if (profile.forbidAdvancedColoring && result.techniqueUsage.containsKey(Technique.ADVANCED_COLORING)) {
            return false;
        }
        if (profile.forbidSwordfish && result.techniqueUsage.containsKey(Technique.SWORDFISH)) {
            return false;
        }

        return true;
    }

    private DifficultyProfile getDifficultyProfile(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> new com.ltp.sudomaster.sudokugen.DifficultyProfile.Builder()
                    .minGivens(EASY_MIN_GIVENS).maxGivens(EASY_MAX_GIVENS)
                    .minCost(EASY_MIN_COST).maxCost(EASY_MAX_COST)
                    .allowedTechniques(Set.of(
                        Technique.SINGLE_CANDIDATE, Technique.SINGLE_POSITION,
                        Technique.NAKED_PAIR, Technique.HIDDEN_PAIR
                    ))
                    .forbidAdvanced(true)
                    .forbidSwordfish(true)
                    .forbidAdvancedColoring(true)
                    .forbidForcingChain(true)
                    .forbidGuessing(true)
                    .build();

            case MEDIUM -> new com.ltp.sudomaster.sudokugen.DifficultyProfile.Builder()
                    .minGivens(MEDIUM_MIN_GIVENS).maxGivens(MEDIUM_MAX_GIVENS)
                    .minCost(MEDIUM_MIN_COST).maxCost(MEDIUM_MAX_COST)
                    .allowedTechniques(Set.of(
                        Technique.SINGLE_CANDIDATE, Technique.SINGLE_POSITION,
                        Technique.NAKED_PAIR, Technique.HIDDEN_PAIR,
                        Technique.POINTING_PAIR, Technique.BOX_LINE,
                        Technique.NAKED_TRIPLE, Technique.HIDDEN_TRIPLE
                    ))
                    .forbidSwordfish(true)
                    .forbidAdvancedColoring(true)
                    .forbidForcingChain(true)
                    .forbidGuessing(true)
                    .build();

            case HARD -> new com.ltp.sudomaster.sudokugen.DifficultyProfile.Builder()
                    .minGivens(HARD_MIN_GIVENS).maxGivens(HARD_MAX_GIVENS)
                    .minCost(HARD_MIN_COST).maxCost(HARD_MAX_COST)
                    .allowedTechniques(Set.of(
                        Technique.SINGLE_CANDIDATE, Technique.SINGLE_POSITION,
                        Technique.NAKED_PAIR, Technique.HIDDEN_PAIR,
                        Technique.POINTING_PAIR, Technique.BOX_LINE,
                        Technique.NAKED_TRIPLE, Technique.HIDDEN_TRIPLE,
                        Technique.X_WING, Technique.XY_WING, Technique.COLORING,
                        Technique.SWORDFISH, Technique.ADVANCED_COLORING, Technique.FORCING_CHAIN
                    ))
                    .forbidGuessing(true)
                    .build();

            case INSANE -> new com.ltp.sudomaster.sudokugen.DifficultyProfile.Builder()
                    .minGivens(INSANE_MIN_GIVENS).maxGivens(INSANE_MAX_GIVENS)
                    .minCost(INSANE_MIN_COST).maxCost(INSANE_MAX_COST)
                    .allowedTechniques(Set.of(Technique.values()))
                    .build();
        };
    }

    private int countGivens(int[][] puzzle) {
        int count = 0;
        for (int[] row : puzzle) {
            for (int cell : row) {
                if (cell != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private int[][] deepCopy(int[][] board) {
        int[][] copy = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, SIZE);
        }
        return copy;
    }
}
