package com.ltp.sudomaster.pointsengine;

import com.ltp.sudomaster.entity.Enums;
import org.springframework.stereotype.Service;

@Service
public class ScoringEngine {

    private static final int BASE_POINTS_EASY = 1000;
    private static final int BASE_POINTS_MEDIUM = 3000;
    private static final int BASE_POINTS_HARD = 10000;
    private static final int BASE_POINTS_INSANE = 25000;

    private static final double TIME_FACTOR_EASY = 8.0;
    private static final double TIME_FACTOR_MEDIUM = 15.0;
    private static final double TIME_FACTOR_HARD = 30.0;
    private static final double TIME_FACTOR_INSANE = 60.0;

    private static final double ASSIST_PENALTY_AUTO_CANDIDATE = 0.4;

    public int calculateScore(
            Enums.Difficulty difficulty,
            Integer elapsedTimeSeconds,
            Integer mistakes,
            Boolean autoCandidateMode) {

        int basePoints = getBasePoints(difficulty);
        double timeMultiplier = calculateTimeMultiplier(difficulty, elapsedTimeSeconds);
        double assistPenalty = calculateAssistPenalty(autoCandidateMode);
        double mistakePenalty = calculateMistakePenalty(mistakes);

        double rawScore = basePoints * timeMultiplier * assistPenalty * mistakePenalty;
        return Math.max(0, (int) Math.round(rawScore));
    }

    private int getBasePoints(Enums.Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> BASE_POINTS_EASY;
            case MEDIUM -> BASE_POINTS_MEDIUM;
            case HARD -> BASE_POINTS_HARD;
            case INSANE -> BASE_POINTS_INSANE;
        };
    }

    private double calculateTimeMultiplier(Enums.Difficulty difficulty, Integer elapsedTimeSeconds) {
        double timeInMinutes = elapsedTimeSeconds / 60.0;
        double timeFactor = getTimeFactor(difficulty);
        double multiplier = 1.8 - (timeInMinutes / timeFactor);
        return Math.max(0.3, multiplier);
    }

    private double getTimeFactor(Enums.Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> TIME_FACTOR_EASY;
            case MEDIUM -> TIME_FACTOR_MEDIUM;
            case HARD -> TIME_FACTOR_HARD;
            case INSANE -> TIME_FACTOR_INSANE;
        };
    }

    private double calculateAssistPenalty(Boolean autoCandidateMode) {
        if (autoCandidateMode != null && autoCandidateMode) {
            return ASSIST_PENALTY_AUTO_CANDIDATE;
        }
        return 1.0;
    }

    private double calculateMistakePenalty(Integer mistakes) {
        if (mistakes == null || mistakes <= 0) {
            return 1.0;
        }

        return switch (Math.min(mistakes, 5)) {
            case 0 -> 1.00;
            case 1 -> 0.95;
            case 2 -> 0.90;
            case 3 -> 0.80;
            case 4 -> 0.65;
            default -> 0.50;
        };
    }
}
