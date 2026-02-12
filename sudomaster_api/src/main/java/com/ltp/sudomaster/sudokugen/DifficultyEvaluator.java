package com.ltp.sudomaster.sudokugen;

import java.util.*;

public class DifficultyEvaluator {

    private static final int SIZE = 9;
    private static final int BLOCK_SIZE = 3;
    private static final int MAX_ITERATIONS = 1000;
    private static final long MAX_EVALUATION_MS = 30000;

    private final int[][] puzzle;
    private final Map<Technique, Integer> techniqueUsage = new EnumMap<>(Technique.class);
    private int score = 0;

    public DifficultyEvaluator(int[][] puzzle) {
        this.puzzle = deepCopy(puzzle);
    }

    public DifficultyResult evaluate() {
        long startTime = System.currentTimeMillis();
        int[][] candidates = buildCandidates(puzzle);
        int iteration = 0;

        while (iteration < MAX_ITERATIONS && System.currentTimeMillis() - startTime < MAX_EVALUATION_MS) {
            boolean progress = false;

            if (applySingleCandidates(candidates)) progress = true;
            if (applySinglePositions(candidates)) progress = true;
            if (applyNakedPairs(candidates)) progress = true;
            if (applyHiddenPairs(candidates)) progress = true;
            if (applyNakedTriples(candidates)) progress = true;
            if (applyHiddenTriples(candidates)) progress = true;
            if (applyPointingPairs(candidates)) progress = true;
            if (applyBoxLineReduction(candidates)) progress = true;
            if (applyXWing(candidates)) progress = true;
            if (applyXYWing(candidates)) progress = true;
            if (applyColoring(candidates)) progress = true;
            if (applySwordfish(candidates)) progress = true;
            if (applyAdvancedColoring(candidates)) progress = true;
            if (applyForcingChain(candidates)) progress = true;

            if (!progress) {
                if (!isSolved(candidates)) {
                    techniqueUsage.merge(Technique.GUESSING, 1, (a, b) -> a + b);
                }
                break;
            }

            iteration++;
        }

        calculateScore();
        Difficulty difficulty = scoreToDifficulty();
        return new DifficultyResult(score, difficulty, techniqueUsage, puzzle);
    }

    private boolean applySingleCandidates(int[][] candidates) {
        boolean changed = false;
        boolean foundFirst = false;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (Integer.bitCount(candidates[r][c]) == 1) {
                    int val = Integer.numberOfTrailingZeros(candidates[r][c]) + 1;
                    if (propagate(candidates, r, c, val)) {
                        if (!foundFirst) {
                            techniqueUsage.merge(Technique.SINGLE_CANDIDATE, 1, (a, b) -> a + b);
                            foundFirst = true;
                        }
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    private boolean applySinglePositions(int[][] candidates) {
        boolean changed = false;

        for (int r = 0; r < SIZE; r++) {
            for (int digit = 1; digit <= SIZE; digit++) {
                int count = 0, lastC = -1;
                for (int c = 0; c < SIZE; c++) {
                    if ((candidates[r][c] & (1 << (digit - 1))) != 0) {
                        count++;
                        lastC = c;
                    }
                }
                if (count == 1) {
                    int before = candidates[r][lastC];
                    candidates[r][lastC] = 1 << (digit - 1);
                    if (propagate(candidates, r, lastC, digit) || before != candidates[r][lastC]) {
                        techniqueUsage.merge(Technique.SINGLE_POSITION, 1, (a, b) -> a + b);
                        changed = true;
                    }
                }
            }
        }

        for (int c = 0; c < SIZE; c++) {
            for (int digit = 1; digit <= SIZE; digit++) {
                int count = 0, lastR = -1;
                for (int r = 0; r < SIZE; r++) {
                    if ((candidates[r][c] & (1 << (digit - 1))) != 0) {
                        count++;
                        lastR = r;
                    }
                }
                if (count == 1) {
                    int before = candidates[lastR][c];
                    candidates[lastR][c] = 1 << (digit - 1);
                    if (propagate(candidates, lastR, c, digit) || before != candidates[lastR][c]) {
                        techniqueUsage.merge(Technique.SINGLE_POSITION, 1, (a, b) -> a + b);
                        changed = true;
                    }
                }
            }
        }

        for (int block = 0; block < SIZE; block++) {
            int br = (block / BLOCK_SIZE) * BLOCK_SIZE;
            int bc = (block % BLOCK_SIZE) * BLOCK_SIZE;
            for (int digit = 1; digit <= SIZE; digit++) {
                int count = 0, lastR = -1, lastC = -1;
                for (int r = br; r < br + BLOCK_SIZE; r++) {
                    for (int c = bc; c < bc + BLOCK_SIZE; c++) {
                        if ((candidates[r][c] & (1 << (digit - 1))) != 0) {
                            count++;
                            lastR = r;
                            lastC = c;
                        }
                    }
                }
                if (count == 1) {
                    int before = candidates[lastR][lastC];
                    candidates[lastR][lastC] = 1 << (digit - 1);
                    if (propagate(candidates, lastR, lastC, digit) || before != candidates[lastR][lastC]) {
                        techniqueUsage.merge(Technique.SINGLE_POSITION, 1, (a, b) -> a + b);
                        changed = true;
                    }
                }
            }
        }

        return changed;
    }

    private boolean applyNakedTriples(int[][] candidates) {
        boolean changed = false;

        for (int r = 0; r < SIZE; r++) {
            changed |= findNakedTriplesInUnit(candidates, r, -1, true);
        }

        for (int c = 0; c < SIZE; c++) {
            changed |= findNakedTriplesInUnit(candidates, -1, c, false);
        }

        for (int block = 0; block < SIZE; block++) {
            int br = (block / BLOCK_SIZE) * BLOCK_SIZE;
            int bc = (block % BLOCK_SIZE) * BLOCK_SIZE;
            changed |= findNakedTriplesInBlock(candidates, br, bc);
        }

        return changed;
    }

    private boolean findNakedTriplesInUnit(int[][] candidates, int r, int c, boolean isRow) {
        boolean changed = false;
        List<int[]> cells = new ArrayList<>();

        if (isRow) {
            for (int cc = 0; cc < SIZE; cc++) {
                if (Integer.bitCount(candidates[r][cc]) >= 2 && Integer.bitCount(candidates[r][cc]) <= 3) {
                    cells.add(new int[]{r, cc});
                }
            }
        } else {
            for (int rr = 0; rr < SIZE; rr++) {
                if (Integer.bitCount(candidates[rr][c]) >= 2 && Integer.bitCount(candidates[rr][c]) <= 3) {
                    cells.add(new int[]{rr, c});
                }
            }
        }

        for (int i = 0; i < cells.size(); i++) {
            for (int j = i + 1; j < cells.size(); j++) {
                for (int k = j + 1; k < cells.size(); k++) {
                    int[] pos_i = cells.get(i);
                    int[] pos_j = cells.get(j);
                    int[] pos_k = cells.get(k);
                    int mask = candidates[pos_i[0]][pos_i[1]] | candidates[pos_j[0]][pos_j[1]] | candidates[pos_k[0]][pos_k[1]];
                    if (Integer.bitCount(mask) == 3) {
                        for (int n = 0; n < cells.size(); n++) {
                            if (n != i && n != j && n != k) {
                                int[] pos = cells.get(n);
                                int before = candidates[pos[0]][pos[1]];
                                candidates[pos[0]][pos[1]] &= ~mask;
                                if (before != candidates[pos[0]][pos[1]]) {
                                    techniqueUsage.merge(Technique.NAKED_TRIPLE, 1, (a, b) -> a + b);
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return changed;
    }

    private boolean findNakedTriplesInBlock(int[][] candidates, int br, int bc) {
        boolean changed = false;
        List<int[]> cells = new ArrayList<>();

        for (int r = br; r < br + BLOCK_SIZE; r++) {
            for (int c = bc; c < bc + BLOCK_SIZE; c++) {
                if (Integer.bitCount(candidates[r][c]) >= 2 && Integer.bitCount(candidates[r][c]) <= 3) {
                    cells.add(new int[]{r, c});
                }
            }
        }

        for (int i = 0; i < cells.size(); i++) {
            for (int j = i + 1; j < cells.size(); j++) {
                for (int k = j + 1; k < cells.size(); k++) {
                    int[] pos_i = cells.get(i);
                    int[] pos_j = cells.get(j);
                    int[] pos_k = cells.get(k);
                    int mask = candidates[pos_i[0]][pos_i[1]] | candidates[pos_j[0]][pos_j[1]] | candidates[pos_k[0]][pos_k[1]];
                    if (Integer.bitCount(mask) == 3) {
                        for (int n = 0; n < cells.size(); n++) {
                            if (n != i && n != j && n != k) {
                                int r = cells.get(n)[0];
                                int c = cells.get(n)[1];
                                int before = candidates[r][c];
                                candidates[r][c] &= ~mask;
                                if (before != candidates[r][c]) {
                                    techniqueUsage.merge(Technique.NAKED_TRIPLE, 1, (a, b) -> a + b);
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return changed;
    }

    private boolean applyNakedPairs(int[][] candidates) {
        boolean changed = false;

        for (int r = 0; r < SIZE; r++) {
            for (int c1 = 0; c1 < SIZE; c1++) {
                if (Integer.bitCount(candidates[r][c1]) != 2) continue;
                for (int c2 = c1 + 1; c2 < SIZE; c2++) {
                    if (candidates[r][c1] == candidates[r][c2]) {
                        int mask = candidates[r][c1];
                        for (int c = 0; c < SIZE; c++) {
                            if (c != c1 && c != c2) {
                                int before = candidates[r][c];
                                candidates[r][c] &= ~mask;
                                if (before != candidates[r][c]) {
                                    techniqueUsage.merge(Technique.NAKED_PAIR, 1, (a, b) -> a + b);
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int c = 0; c < SIZE; c++) {
            for (int r1 = 0; r1 < SIZE; r1++) {
                if (Integer.bitCount(candidates[r1][c]) != 2) continue;
                for (int r2 = r1 + 1; r2 < SIZE; r2++) {
                    if (candidates[r1][c] == candidates[r2][c]) {
                        int mask = candidates[r1][c];
                        for (int r = 0; r < SIZE; r++) {
                            if (r != r1 && r != r2) {
                                int before = candidates[r][c];
                                candidates[r][c] &= ~mask;
                                if (before != candidates[r][c]) {
                                    techniqueUsage.merge(Technique.NAKED_PAIR, 1, (a, b) -> a + b);
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int block = 0; block < SIZE; block++) {
            int br = (block / BLOCK_SIZE) * BLOCK_SIZE;
            int bc = (block % BLOCK_SIZE) * BLOCK_SIZE;
            for (int r1 = br; r1 < br + BLOCK_SIZE; r1++) {
                for (int c1 = bc; c1 < bc + BLOCK_SIZE; c1++) {
                    if (Integer.bitCount(candidates[r1][c1]) != 2) continue;
                    for (int r2 = br; r2 < br + BLOCK_SIZE; r2++) {
                        for (int c2 = bc; c2 < bc + BLOCK_SIZE; c2++) {
                            if ((r1 != r2 || c1 != c2) && candidates[r1][c1] == candidates[r2][c2]) {
                                int mask = candidates[r1][c1];
                                for (int r = br; r < br + BLOCK_SIZE; r++) {
                                    for (int c = bc; c < bc + BLOCK_SIZE; c++) {
                                        if ((r != r1 || c != c1) && (r != r2 || c != c2)) {
                                            int before = candidates[r][c];
                                            candidates[r][c] &= ~mask;
                                            if (before != candidates[r][c]) {
                                                techniqueUsage.merge(Technique.NAKED_PAIR, 1, (a, b) -> a + b);
                                                changed = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return changed;
    }

    private boolean applyHiddenPairs(int[][] candidates) {
        boolean changed = false;

        for (int r = 0; r < SIZE; r++) {
            changed |= findHiddenPairsInUnit(candidates, r, -1, true);
        }

        for (int c = 0; c < SIZE; c++) {
            changed |= findHiddenPairsInUnit(candidates, -1, c, false);
        }

        for (int block = 0; block < SIZE; block++) {
            int br = (block / BLOCK_SIZE) * BLOCK_SIZE;
            int bc = (block % BLOCK_SIZE) * BLOCK_SIZE;
            changed |= findHiddenPairsInBlock(candidates, br, bc);
        }

        return changed;
    }

    private boolean findHiddenPairsInUnit(int[][] candidates, int r, int c, boolean isRow) {
        boolean changed = false;
        for (int d1 = 1; d1 < SIZE; d1++) {
            for (int d2 = d1 + 1; d2 <= SIZE; d2++) {
                int m1 = 1 << (d1 - 1);
                int m2 = 1 << (d2 - 1);
                int mask = m1 | m2;
                List<int[]> positions = new ArrayList<>();

                if (isRow) {
                    for (int cc = 0; cc < SIZE; cc++) {
                        if (((candidates[r][cc] & m1) != 0 || (candidates[r][cc] & m2) != 0)) {
                            positions.add(new int[]{r, cc});
                        }
                    }
                } else {
                    for (int rr = 0; rr < SIZE; rr++) {
                        if (((candidates[rr][c] & m1) != 0 || (candidates[rr][c] & m2) != 0)) {
                            positions.add(new int[]{rr, c});
                        }
                    }
                }

                if (positions.size() == 2) {
                    for (int[] pos : positions) {
                        int before = candidates[pos[0]][pos[1]];
                        candidates[pos[0]][pos[1]] &= mask;
                        if (before != candidates[pos[0]][pos[1]]) {
                            techniqueUsage.merge(Technique.HIDDEN_PAIR, 1, (a, b) -> a + b);
                            changed = true;
                        }
                    }
                }
            }
        }
        return changed;
    }

    private boolean findHiddenPairsInBlock(int[][] candidates, int br, int bc) {
        boolean changed = false;
        for (int d1 = 1; d1 < SIZE; d1++) {
            for (int d2 = d1 + 1; d2 <= SIZE; d2++) {
                int m1 = 1 << (d1 - 1);
                int m2 = 1 << (d2 - 1);
                int mask = m1 | m2;
                List<int[]> positions = new ArrayList<>();

                for (int r = br; r < br + BLOCK_SIZE; r++) {
                    for (int c = bc; c < bc + BLOCK_SIZE; c++) {
                        if (((candidates[r][c] & m1) != 0 || (candidates[r][c] & m2) != 0)) {
                            positions.add(new int[]{r, c});
                        }
                    }
                }

                if (positions.size() == 2) {
                    for (int[] pos : positions) {
                        int before = candidates[pos[0]][pos[1]];
                        candidates[pos[0]][pos[1]] &= mask;
                        if (before != candidates[pos[0]][pos[1]]) {
                            techniqueUsage.merge(Technique.HIDDEN_PAIR, 1, (a, b) -> a + b);
                            changed = true;
                        }
                    }
                }
            }
        }
        return changed;
    }

    private boolean applyHiddenTriples(int[][] candidates) {
        boolean changed = false;

        for (int r = 0; r < SIZE; r++) {
            changed |= findHiddenTriplesInUnit(candidates, r, -1, true);
        }

        for (int c = 0; c < SIZE; c++) {
            changed |= findHiddenTriplesInUnit(candidates, -1, c, false);
        }

        for (int block = 0; block < SIZE; block++) {
            int br = (block / BLOCK_SIZE) * BLOCK_SIZE;
            int bc = (block % BLOCK_SIZE) * BLOCK_SIZE;
            changed |= findHiddenTriplesInBlock(candidates, br, bc);
        }

        return changed;
    }

    private boolean findHiddenTriplesInUnit(int[][] candidates, int r, int c, boolean isRow) {
        boolean changed = false;

        for (int d1 = 1; d1 < SIZE; d1++) {
            for (int d2 = d1 + 1; d2 < SIZE; d2++) {
                for (int d3 = d2 + 1; d3 <= SIZE; d3++) {
                    int m1 = 1 << (d1 - 1);
                    int m2 = 1 << (d2 - 1);
                    int m3 = 1 << (d3 - 1);
                    int mask = m1 | m2 | m3;
                    List<int[]> positions = new ArrayList<>();

                    if (isRow) {
                        for (int cc = 0; cc < SIZE; cc++) {
                            if (((candidates[r][cc] & m1) != 0 || (candidates[r][cc] & m2) != 0 || (candidates[r][cc] & m3) != 0)) {
                                positions.add(new int[]{r, cc});
                            }
                        }
                    } else {
                        for (int rr = 0; rr < SIZE; rr++) {
                            if (((candidates[rr][c] & m1) != 0 || (candidates[rr][c] & m2) != 0 || (candidates[rr][c] & m3) != 0)) {
                                positions.add(new int[]{rr, c});
                            }
                        }
                    }

                    if (positions.size() == 3) {
                        for (int[] pos : positions) {
                            int before = candidates[pos[0]][pos[1]];
                            candidates[pos[0]][pos[1]] &= mask;
                            if (before != candidates[pos[0]][pos[1]]) {
                                techniqueUsage.merge(Technique.HIDDEN_TRIPLE, 1, (a, b) -> a + b);
                                changed = true;
                            }
                        }
                    }
                }
            }
        }
        return changed;
    }

    private boolean findHiddenTriplesInBlock(int[][] candidates, int br, int bc) {
        boolean changed = false;

        for (int d1 = 1; d1 < SIZE; d1++) {
            for (int d2 = d1 + 1; d2 < SIZE; d2++) {
                for (int d3 = d2 + 1; d3 <= SIZE; d3++) {
                    int m1 = 1 << (d1 - 1);
                    int m2 = 1 << (d2 - 1);
                    int m3 = 1 << (d3 - 1);
                    int mask = m1 | m2 | m3;
                    List<int[]> positions = new ArrayList<>();

                    for (int r = br; r < br + BLOCK_SIZE; r++) {
                        for (int c = bc; c < bc + BLOCK_SIZE; c++) {
                            if (((candidates[r][c] & m1) != 0 || (candidates[r][c] & m2) != 0 || (candidates[r][c] & m3) != 0)) {
                                positions.add(new int[]{r, c});
                            }
                        }
                    }

                    if (positions.size() == 3) {
                        for (int[] pos : positions) {
                            int before = candidates[pos[0]][pos[1]];
                            candidates[pos[0]][pos[1]] &= mask;
                            if (before != candidates[pos[0]][pos[1]]) {
                                techniqueUsage.merge(Technique.HIDDEN_TRIPLE, 1, (a, b) -> a + b);
                                changed = true;
                            }
                        }
                    }
                }
            }
        }
        return changed;
    }

    private boolean applyPointingPairs(int[][] candidates) {
        boolean changed = false;

        for (int box = 0; box < SIZE; box++) {
            int br = (box / BLOCK_SIZE) * BLOCK_SIZE;
            int bc = (box % BLOCK_SIZE) * BLOCK_SIZE;

            for (int digit = 1; digit <= SIZE; digit++) {
                int mask = 1 << (digit - 1);
                Set<Integer> rows = new HashSet<>();
                Set<Integer> cols = new HashSet<>();

                for (int r = br; r < br + BLOCK_SIZE; r++) {
                    for (int c = bc; c < bc + BLOCK_SIZE; c++) {
                        if ((candidates[r][c] & mask) != 0) {
                            rows.add(r);
                            cols.add(c);
                        }
                    }
                }

                if (rows.size() == 1) {
                    int r = rows.iterator().next();
                    for (int c = 0; c < bc; c++) {
                        int before = candidates[r][c];
                        candidates[r][c] &= ~mask;
                        if (before != candidates[r][c]) {
                            techniqueUsage.merge(Technique.POINTING_PAIR, 1, (a, b) -> a + b);
                            changed = true;
                        }
                    }
                    for (int c = bc + BLOCK_SIZE; c < SIZE; c++) {
                        int before = candidates[r][c];
                        candidates[r][c] &= ~mask;
                        if (before != candidates[r][c]) {
                            techniqueUsage.merge(Technique.POINTING_PAIR, 1, (a, b) -> a + b);
                            changed = true;
                        }
                    }
                }

                if (cols.size() == 1) {
                    int c = cols.iterator().next();
                    for (int r = 0; r < br; r++) {
                        int before = candidates[r][c];
                        candidates[r][c] &= ~mask;
                        if (before != candidates[r][c]) {
                            techniqueUsage.merge(Technique.POINTING_PAIR, 1, (a, b) -> a + b);
                            changed = true;
                        }
                    }
                    for (int r = br + BLOCK_SIZE; r < SIZE; r++) {
                        int before = candidates[r][c];
                        candidates[r][c] &= ~mask;
                        if (before != candidates[r][c]) {
                            techniqueUsage.merge(Technique.POINTING_PAIR, 1, (a, b) -> a + b);
                            changed = true;
                        }
                    }
                }
            }
        }

        return changed;
    }

    private boolean applyBoxLineReduction(int[][] candidates) {
        boolean changed = false;

        for (int r = 0; r < SIZE; r++) {
            for (int digit = 1; digit <= SIZE; digit++) {
                int mask = 1 << (digit - 1);
                Set<Integer> boxes = new HashSet<>();
                for (int c = 0; c < SIZE; c++) {
                    if ((candidates[r][c] & mask) != 0) {
                        boxes.add(c / BLOCK_SIZE);
                    }
                }

                if (boxes.size() == 1) {
                    int box = boxes.iterator().next();
                    int br = (r / BLOCK_SIZE) * BLOCK_SIZE;
                    int bc = box * BLOCK_SIZE;

                    for (int rr = br; rr < br + BLOCK_SIZE; rr++) {
                        if (rr != r) {
                            for (int cc = bc; cc < bc + BLOCK_SIZE; cc++) {
                                int before = candidates[rr][cc];
                                candidates[rr][cc] &= ~mask;
                                if (before != candidates[rr][cc]) {
                                    techniqueUsage.merge(Technique.BOX_LINE, 1, (a, b) -> a + b);
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int c = 0; c < SIZE; c++) {
            for (int digit = 1; digit <= SIZE; digit++) {
                int mask = 1 << (digit - 1);
                Set<Integer> boxes = new HashSet<>();
                for (int r = 0; r < SIZE; r++) {
                    if ((candidates[r][c] & mask) != 0) {
                        boxes.add(r / BLOCK_SIZE);
                    }
                }

                if (boxes.size() == 1) {
                    int box = boxes.iterator().next();
                    int br = box * BLOCK_SIZE;
                    int bc = (c / BLOCK_SIZE) * BLOCK_SIZE;

                    for (int rr = br; rr < br + BLOCK_SIZE; rr++) {
                        for (int cc = bc; cc < bc + BLOCK_SIZE; cc++) {
                            if (cc != c) {
                                int before = candidates[rr][cc];
                                candidates[rr][cc] &= ~mask;
                                if (before != candidates[rr][cc]) {
                                    techniqueUsage.merge(Technique.BOX_LINE, 1, (a, b) -> a + b);
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return changed;
    }

    private boolean applyXWing(int[][] candidates) {
        boolean changed = false;

        for (int digit = 1; digit <= SIZE; digit++) {
            int mask = 1 << (digit - 1);

            for (int r1 = 0; r1 < SIZE; r1++) {
                for (int r2 = r1 + 1; r2 < SIZE; r2++) {
                    List<Integer> cols1 = new ArrayList<>();
                    List<Integer> cols2 = new ArrayList<>();

                    for (int c = 0; c < SIZE; c++) {
                        if ((candidates[r1][c] & mask) != 0) cols1.add(c);
                        if ((candidates[r2][c] & mask) != 0) cols2.add(c);
                    }

                    if (cols1.size() == 2 && cols1.equals(cols2)) {
                        int c1 = cols1.get(0);
                        int c2 = cols1.get(1);

                        for (int r = 0; r < SIZE; r++) {
                            if (r != r1 && r != r2) {
                                int before1 = candidates[r][c1];
                                candidates[r][c1] &= ~mask;
                                int before2 = candidates[r][c2];
                                candidates[r][c2] &= ~mask;
                                if (before1 != candidates[r][c1] || before2 != candidates[r][c2]) {
                                    techniqueUsage.merge(Technique.X_WING, 1, (a, b) -> a + b);
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }

            for (int c1 = 0; c1 < SIZE; c1++) {
                for (int c2 = c1 + 1; c2 < SIZE; c2++) {
                    List<Integer> rows1 = new ArrayList<>();
                    List<Integer> rows2 = new ArrayList<>();

                    for (int r = 0; r < SIZE; r++) {
                        if ((candidates[r][c1] & mask) != 0) rows1.add(r);
                        if ((candidates[r][c2] & mask) != 0) rows2.add(r);
                    }

                    if (rows1.size() == 2 && rows1.equals(rows2)) {
                        int r1 = rows1.get(0);
                        int r2 = rows1.get(1);

                        for (int c = 0; c < SIZE; c++) {
                            if (c != c1 && c != c2) {
                                int before1 = candidates[r1][c];
                                candidates[r1][c] &= ~mask;
                                int before2 = candidates[r2][c];
                                candidates[r2][c] &= ~mask;
                                if (before1 != candidates[r1][c] || before2 != candidates[r2][c]) {
                                    techniqueUsage.merge(Technique.X_WING, 1, (a, b) -> a + b);
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return changed;
    }

    private boolean applySwordfish(int[][] candidates) {
        boolean changed = false;

        for (int digit = 1; digit <= SIZE; digit++) {
            int mask = 1 << (digit - 1);

            for (int r1 = 0; r1 < SIZE; r1++) {
                for (int r2 = r1 + 1; r2 < SIZE; r2++) {
                    for (int r3 = r2 + 1; r3 < SIZE; r3++) {
                        List<Integer> cols1 = new ArrayList<>();
                        List<Integer> cols2 = new ArrayList<>();
                        List<Integer> cols3 = new ArrayList<>();

                        for (int c = 0; c < SIZE; c++) {
                            if ((candidates[r1][c] & mask) != 0) cols1.add(c);
                            if ((candidates[r2][c] & mask) != 0) cols2.add(c);
                            if ((candidates[r3][c] & mask) != 0) cols3.add(c);
                        }

                        if (cols1.size() == 3 && cols1.equals(cols2) && cols1.equals(cols3)) {
                            for (int r = 0; r < SIZE; r++) {
                                if (r != r1 && r != r2 && r != r3) {
                                    for (int c : cols1) {
                                        int before = candidates[r][c];
                                        candidates[r][c] &= ~mask;
                                        if (before != candidates[r][c]) {
                                            techniqueUsage.merge(Technique.SWORDFISH, 1, (a, b) -> a + b);
                                            changed = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (int c1 = 0; c1 < SIZE; c1++) {
                for (int c2 = c1 + 1; c2 < SIZE; c2++) {
                    for (int c3 = c2 + 1; c3 < SIZE; c3++) {
                        List<Integer> rows1 = new ArrayList<>();
                        List<Integer> rows2 = new ArrayList<>();
                        List<Integer> rows3 = new ArrayList<>();

                        for (int r = 0; r < SIZE; r++) {
                            if ((candidates[r][c1] & mask) != 0) rows1.add(r);
                            if ((candidates[r][c2] & mask) != 0) rows2.add(r);
                            if ((candidates[r][c3] & mask) != 0) rows3.add(r);
                        }

                        if (rows1.size() == 3 && rows1.equals(rows2) && rows1.equals(rows3)) {
                            for (int c = 0; c < SIZE; c++) {
                                if (c != c1 && c != c2 && c != c3) {
                                    for (int r : rows1) {
                                        int before = candidates[r][c];
                                        candidates[r][c] &= ~mask;
                                        if (before != candidates[r][c]) {
                                            techniqueUsage.merge(Technique.SWORDFISH, 1, (a, b) -> a + b);
                                            changed = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return changed;
    }

    private boolean applyXYWing(int[][] candidates) {
        boolean changed = false;
        boolean foundOne = false;

        for (int ar = 0; ar < SIZE && !foundOne; ar++) {
            for (int ac = 0; ac < SIZE && !foundOne; ac++) {
                if (Integer.bitCount(candidates[ar][ac]) != 2) continue;
                
                int pivotMask = candidates[ar][ac];
                int[] pivotDigits = new int[2];
                int idx = 0;
                for (int bit = 0; bit < SIZE; bit++) {
                    if ((pivotMask & (1 << bit)) != 0) {
                        pivotDigits[idx++] = bit;
                    }
                }

                int x = pivotDigits[0];
                int y = pivotDigits[1];
                int maskX = 1 << x;
                int maskY = 1 << y;

                for (int br = 0; br < SIZE && !foundOne; br++) {
                    for (int bc = 0; bc < SIZE && !foundOne; bc++) {
                        if ((br == ar && bc == ac) || Integer.bitCount(candidates[br][bc]) != 2) continue;
                        if (!sees(ar, ac, br, bc)) continue;

                        if (((candidates[br][bc] & maskX) == 0) || ((candidates[br][bc] & maskY) != 0)) continue;

                        int wing1Mask = candidates[br][bc];
                        int z = -1;
                        for (int bit = 0; bit < SIZE; bit++) {
                            if ((wing1Mask & (1 << bit)) != 0 && bit != x) {
                                z = bit;
                                break;
                            }
                        }

                        if (z == -1) continue;
                        int maskZ = 1 << z;

                        for (int cr = 0; cr < SIZE; cr++) {
                            for (int cc = 0; cc < SIZE; cc++) {
                                if ((cr == ar && cc == ac) || (cr == br && cc == bc)) continue;
                                if (Integer.bitCount(candidates[cr][cc]) != 2) continue;
                                if (!sees(ar, ac, cr, cc) || !sees(br, bc, cr, cc)) continue;

                                if (((candidates[cr][cc] & maskY) == 0) || 
                                    ((candidates[cr][cc] & maskZ) == 0) ||
                                    ((candidates[cr][cc] & maskX) != 0)) continue;

                                for (int er = 0; er < SIZE; er++) {
                                    for (int ec = 0; ec < SIZE; ec++) {
                                        if ((er == ar && ec == ac) || (er == br && ec == bc) || (er == cr && ec == cc)) continue;
                                        if (sees(br, bc, er, ec) && sees(cr, cc, er, ec)) {
                                            int before = candidates[er][ec];
                                            candidates[er][ec] &= ~maskZ;
                                            if (before != candidates[er][ec]) {
                                                techniqueUsage.merge(Technique.XY_WING, 1, (a, b) -> a + b);
                                                foundOne = true;
                                                changed = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return changed;
    }

    private boolean applyColoring(int[][] candidates) {
        boolean changed = false;
        boolean foundOne = false;

        for (int digit = 1; digit <= SIZE && !foundOne; digit++) {
            int mask = 1 << (digit - 1);
            int[] color = new int[SIZE * SIZE];

            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    if ((candidates[r][c] & mask) == 0 || Integer.bitCount(candidates[r][c]) != 2) continue;

                    int idx = r * SIZE + c;
                    if (color[idx] != 0) continue;

                    Queue<Integer> queue = new LinkedList<>();
                    queue.add(idx);
                    color[idx] = 1;

                    while (!queue.isEmpty()) {
                        int current = queue.poll();
                        int cr = current / SIZE;
                        int cc = current % SIZE;
                        int nextColor = color[current] == 1 ? 2 : 1;

                        int count = 0;
                        for (int c2 = 0; c2 < SIZE; c2++) {
                            if ((candidates[cr][c2] & mask) != 0 && Integer.bitCount(candidates[cr][c2]) >= 2) {
                                count++;
                            }
                        }
                        if (count == 2) {
                            for (int c2 = 0; c2 < SIZE; c2++) {
                                if (c2 != cc && (candidates[cr][c2] & mask) != 0 && Integer.bitCount(candidates[cr][c2]) >= 2) {
                                    int idx2 = cr * SIZE + c2;
                                    if (color[idx2] == 0) {
                                        color[idx2] = nextColor;
                                        queue.add(idx2);
                                    }
                                }
                            }
                        }

                        count = 0;
                        for (int r2 = 0; r2 < SIZE; r2++) {
                            if ((candidates[r2][cc] & mask) != 0 && Integer.bitCount(candidates[r2][cc]) >= 2) {
                                count++;
                            }
                        }
                        if (count == 2) {
                            for (int r2 = 0; r2 < SIZE; r2++) {
                                if (r2 != cr && (candidates[r2][cc] & mask) != 0 && Integer.bitCount(candidates[r2][cc]) >= 2) {
                                    int idx2 = r2 * SIZE + cc;
                                    if (color[idx2] == 0) {
                                        color[idx2] = nextColor;
                                        queue.add(idx2);
                                    }
                                }
                            }
                        }

                        int br = (cr / BLOCK_SIZE) * BLOCK_SIZE;
                        int bc = (cc / BLOCK_SIZE) * BLOCK_SIZE;
                        count = 0;
                        for (int r2 = br; r2 < br + BLOCK_SIZE; r2++) {
                            for (int c2 = bc; c2 < bc + BLOCK_SIZE; c2++) {
                                if ((candidates[r2][c2] & mask) != 0 && Integer.bitCount(candidates[r2][c2]) >= 2) {
                                    count++;
                                }
                            }
                        }
                        if (count == 2) {
                            for (int r2 = br; r2 < br + BLOCK_SIZE; r2++) {
                                for (int c2 = bc; c2 < bc + BLOCK_SIZE; c2++) {
                                    if ((r2 != cr || c2 != cc) && (candidates[r2][c2] & mask) != 0 && Integer.bitCount(candidates[r2][c2]) >= 2) {
                                        int idx2 = r2 * SIZE + c2;
                                        if (color[idx2] == 0) {
                                            color[idx2] = nextColor;
                                            queue.add(idx2);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (int r = 0; r < SIZE && !foundOne; r++) {
                for (int c = 0; c < SIZE; c++) {
                    int idx = r * SIZE + c;
                    if ((candidates[r][c] & mask) == 0 || color[idx] == 0) continue;

                    int myColor = color[idx];

                    for (int c2 = c + 1; c2 < SIZE; c2++) {
                        int idx2 = r * SIZE + c2;
                        if ((candidates[r][c2] & mask) != 0 && color[idx2] == myColor) {
                            int otherColor = 3 - myColor;
                            for (int r3 = 0; r3 < SIZE; r3++) {
                                for (int c3 = 0; c3 < SIZE; c3++) {
                                    int idx3 = r3 * SIZE + c3;
                                    if ((candidates[r3][c3] & mask) != 0 && color[idx3] == otherColor) {
                                        int before = candidates[r3][c3];
                                        candidates[r3][c3] &= ~mask;
                                        if (before != candidates[r3][c3]) {
                                            techniqueUsage.merge(Technique.COLORING, 1, (a, b) -> a + b);
                                            foundOne = true;
                                            changed = true;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!foundOne) {
                        for (int r2 = r + 1; r2 < SIZE; r2++) {
                            int idx2 = r2 * SIZE + c;
                            if ((candidates[r2][c] & mask) != 0 && color[idx2] == myColor) {
                                int otherColor = 3 - myColor;
                                for (int r3 = 0; r3 < SIZE; r3++) {
                                    for (int c3 = 0; c3 < SIZE; c3++) {
                                        int idx3 = r3 * SIZE + c3;
                                        if ((candidates[r3][c3] & mask) != 0 && color[idx3] == otherColor) {
                                            int before = candidates[r3][c3];
                                            candidates[r3][c3] &= ~mask;
                                            if (before != candidates[r3][c3]) {
                                                techniqueUsage.merge(Technique.COLORING, 1, (a, b) -> a + b);
                                                foundOne = true;
                                                changed = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!foundOne) {
                        int br = (r / BLOCK_SIZE) * BLOCK_SIZE;
                        int bc = (c / BLOCK_SIZE) * BLOCK_SIZE;
                        for (int r2 = br; r2 < br + BLOCK_SIZE; r2++) {
                            for (int c2 = bc; c2 < bc + BLOCK_SIZE; c2++) {
                                if ((r2 != r || c2 != c)) {
                                    int idx2 = r2 * SIZE + c2;
                                    if ((candidates[r2][c2] & mask) != 0 && color[idx2] == myColor) {
                                        int otherColor = 3 - myColor;
                                        for (int r3 = 0; r3 < SIZE; r3++) {
                                            for (int c3 = 0; c3 < SIZE; c3++) {
                                                int idx3 = r3 * SIZE + c3;
                                                if ((candidates[r3][c3] & mask) != 0 && color[idx3] == otherColor) {
                                                    int before = candidates[r3][c3];
                                                    candidates[r3][c3] &= ~mask;
                                                    if (before != candidates[r3][c3]) {
                                                        techniqueUsage.merge(Technique.COLORING, 1, (a, b) -> a + b);
                                                        foundOne = true;
                                                        changed = true;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return changed;
    }

    private boolean applyAdvancedColoring(int[][] candidates) {
        boolean changed = false;
        boolean foundOne = false;

        for (int digit = 1; digit <= SIZE && !foundOne; digit++) {
            int mask = 1 << (digit - 1);
            int[] color = new int[SIZE * SIZE];

            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    if ((candidates[r][c] & mask) == 0 || Integer.bitCount(candidates[r][c]) != 2) continue;

                    int idx = r * SIZE + c;
                    if (color[idx] != 0) continue;

                    Queue<Integer> queue = new LinkedList<>();
                    queue.add(idx);
                    color[idx] = 1;

                    while (!queue.isEmpty()) {
                        int current = queue.poll();
                        int cr = current / SIZE;
                        int cc = current % SIZE;
                        int nextColor = color[current] == 1 ? 2 : 1;

                        int count = 0;
                        for (int c2 = 0; c2 < SIZE; c2++) {
                            if ((candidates[cr][c2] & mask) != 0) count++;
                        }
                        if (count == 2) {
                            for (int c2 = 0; c2 < SIZE; c2++) {
                                if (c2 != cc && (candidates[cr][c2] & mask) != 0) {
                                    int idx2 = cr * SIZE + c2;
                                    if (color[idx2] == 0) {
                                        color[idx2] = nextColor;
                                        queue.add(idx2);
                                    }
                                }
                            }
                        }

                        count = 0;
                        for (int r2 = 0; r2 < SIZE; r2++) {
                            if ((candidates[r2][cc] & mask) != 0) count++;
                        }
                        if (count == 2) {
                            for (int r2 = 0; r2 < SIZE; r2++) {
                                if (r2 != cr && (candidates[r2][cc] & mask) != 0) {
                                    int idx2 = r2 * SIZE + cc;
                                    if (color[idx2] == 0) {
                                        color[idx2] = nextColor;
                                        queue.add(idx2);
                                    }
                                }
                            }
                        }

                        int br = (cr / BLOCK_SIZE) * BLOCK_SIZE;
                        int bc = (cc / BLOCK_SIZE) * BLOCK_SIZE;
                        count = 0;
                        for (int r2 = br; r2 < br + BLOCK_SIZE; r2++) {
                            for (int c2 = bc; c2 < bc + BLOCK_SIZE; c2++) {
                                if ((candidates[r2][c2] & mask) != 0) count++;
                            }
                        }
                        if (count == 2) {
                            for (int r2 = br; r2 < br + BLOCK_SIZE; r2++) {
                                for (int c2 = bc; c2 < bc + BLOCK_SIZE; c2++) {
                                    if ((r2 != cr || c2 != cc) && (candidates[r2][c2] & mask) != 0) {
                                        int idx2 = r2 * SIZE + c2;
                                        if (color[idx2] == 0) {
                                            color[idx2] = nextColor;
                                            queue.add(idx2);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (int r = 0; r < SIZE && !foundOne; r++) {
                for (int c = 0; c < SIZE; c++) {
                    int idx = r * SIZE + c;
                    if ((candidates[r][c] & mask) == 0 || color[idx] == 0) continue;

                    int myColor = color[idx];
                    
                    for (int c2 = c + 1; c2 < SIZE; c2++) {
                        int idx2 = r * SIZE + c2;
                        if ((candidates[r][c2] & mask) != 0 && color[idx2] == myColor) {
                            int otherColor = 3 - myColor;
                            for (int c3 = 0; c3 < SIZE; c3++) {
                                int idx3 = r * SIZE + c3;
                                if ((candidates[r][c3] & mask) != 0 && color[idx3] == otherColor) {
                                    int before = candidates[r][c3];
                                    candidates[r][c3] &= ~mask;
                                    if (before != candidates[r][c3]) {
                                        techniqueUsage.merge(Technique.ADVANCED_COLORING, 1, (a, b) -> a + b);
                                        foundOne = true;
                                        changed = true;
                                    }
                                }
                            }
                        }
                    }

                    for (int r2 = r + 1; r2 < SIZE && !foundOne; r2++) {
                        int idx2 = r2 * SIZE + c;
                        if ((candidates[r2][c] & mask) != 0 && color[idx2] == myColor) {
                            int otherColor = 3 - myColor;
                            for (int r3 = 0; r3 < SIZE; r3++) {
                                int idx3 = r3 * SIZE + c;
                                if ((candidates[r3][c] & mask) != 0 && color[idx3] == otherColor) {
                                    int before = candidates[r3][c];
                                    candidates[r3][c] &= ~mask;
                                    if (before != candidates[r3][c]) {
                                        techniqueUsage.merge(Technique.ADVANCED_COLORING, 1, (a, b) -> a + b);
                                        foundOne = true;
                                        changed = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return changed;
    }

    private boolean applyForcingChain(int[][] candidates) {
        boolean changed = false;
        boolean foundOne = false;

        for (int r = 0; r < SIZE && !foundOne; r++) {
            for (int c = 0; c < SIZE && !foundOne; c++) {
                int candidateCount = Integer.bitCount(candidates[r][c]);
                if (candidateCount > 1 && candidateCount <= 5) {
                    for (int bit = 0; bit < SIZE; bit++) {
                        if ((candidates[r][c] & (1 << bit)) != 0) {
                            int[][] testCandidates = deepCopy(candidates);
                            testCandidates[r][c] = 1 << bit;
                            
                            boolean valid = propagateChain(testCandidates);
                            
                            if (!valid) {
                                int before = candidates[r][c];
                                candidates[r][c] &= ~(1 << bit);
                                if (before != candidates[r][c]) {
                                    if (!foundOne) {
                                        techniqueUsage.merge(Technique.FORCING_CHAIN, 1, (a, b) -> a + b);
                                        foundOne = true;
                                    }
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return changed;
    }

    private boolean propagateChain(int[][] candidates) {
        boolean changed = true;
        int iterations = 0;
        int maxIterations = 100;

        while (changed && iterations < maxIterations) {
            changed = false;
            iterations++;

            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    if (candidates[r][c] == 0) {
                        return false;
                    }
                }
            }

            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    if (Integer.bitCount(candidates[r][c]) == 1) {
                        int val = Integer.numberOfTrailingZeros(candidates[r][c]) + 1;
                        if (propagate(candidates, r, c, val)) {
                            changed = true;
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean propagate(int[][] candidates, int r, int c, int digit) {
        boolean changed = false;
        int mask = 1 << (digit - 1);

        for (int i = 0; i < SIZE; i++) {
            if (i != c) {
                int before = candidates[r][i];
                candidates[r][i] &= ~mask;
                if (before != candidates[r][i]) changed = true;
            }
        }

        for (int i = 0; i < SIZE; i++) {
            if (i != r) {
                int before = candidates[i][c];
                candidates[i][c] &= ~mask;
                if (before != candidates[i][c]) changed = true;
            }
        }

        int br = (r / BLOCK_SIZE) * BLOCK_SIZE;
        int bc = (c / BLOCK_SIZE) * BLOCK_SIZE;
        for (int i = br; i < br + BLOCK_SIZE; i++) {
            for (int j = bc; j < bc + BLOCK_SIZE; j++) {
                if (i != r || j != c) {
                    int before = candidates[i][j];
                    candidates[i][j] &= ~mask;
                    if (before != candidates[i][j]) changed = true;
                }
            }
        }

        return changed;
    }

    private int[][] buildCandidates(int[][] board) {
        int[][] candidates = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] == 0) {
                    int mask = 0x1FF;
                    for (int i = 0; i < SIZE; i++) {
                        if (board[r][i] != 0) mask &= ~(1 << (board[r][i] - 1));
                        if (board[i][c] != 0) mask &= ~(1 << (board[i][c] - 1));
                    }
                    int br = (r / BLOCK_SIZE) * BLOCK_SIZE;
                    int bc = (c / BLOCK_SIZE) * BLOCK_SIZE;
                    for (int i = br; i < br + BLOCK_SIZE; i++) {
                        for (int j = bc; j < bc + BLOCK_SIZE; j++) {
                            if (board[i][j] != 0) mask &= ~(1 << (board[i][j] - 1));
                        }
                    }
                    candidates[r][c] = mask;
                } else {
                    candidates[r][c] = 1 << (board[r][c] - 1);
                }
            }
        }
        return candidates;
    }

    private boolean isSolved(int[][] candidates) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (Integer.bitCount(candidates[r][c]) != 1) return false;
            }
        }
        return true;
    }

    private void calculateScore() {
        int techniqueDiversity = 0;
        
        for (Map.Entry<Technique, Integer> entry : techniqueUsage.entrySet()) {
            if (entry.getValue() > 0) {
                Technique tech = entry.getKey();
                techniqueDiversity++;
                score += tech.getWeight();
            }
        }
        
        if (techniqueDiversity >= 5) {
            score += 15;
        } else if (techniqueDiversity >= 3) {
            score += 5;
        }
    }

    private Difficulty scoreToDifficulty() {
        int givens = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (puzzle[r][c] != 0) givens++;
            }
        }

        Technique hardestTechnique = Technique.SINGLE_CANDIDATE;
        for (Technique tech : Technique.values()) {
            if (techniqueUsage.containsKey(tech) && techniqueUsage.get(tech) > 0) {
                if (tech.getWeight() > hardestTechnique.getWeight()) {
                    hardestTechnique = tech;
                }
            }
        }

        int hardestWeight = hardestTechnique.getWeight();
        
        if (hardestWeight <= 6) {
            if (givens >= 32) {
                return Difficulty.EASY;
            }
        }

        if (hardestWeight >= 7 && hardestWeight <= 10) {
            if (givens >= 28) {
                return Difficulty.MEDIUM;
            }
        }

        if (hardestWeight >= 18 && hardestWeight <= 30) {
            if (givens >= 22 && !techniqueUsage.containsKey(Technique.SWORDFISH) &&
                !techniqueUsage.containsKey(Technique.ADVANCED_COLORING) &&
                !techniqueUsage.containsKey(Technique.FORCING_CHAIN)) {
                return Difficulty.HARD;
            }
        }

        if (hardestWeight >= 35 && hardestWeight < 65) {
            if (givens >= 17 && !techniqueUsage.containsKey(Technique.FORCING_CHAIN) &&
                !techniqueUsage.containsKey(Technique.GUESSING)) {
                return Difficulty.HARD;
            }
        }

        if (hardestWeight >= 65 || 
            techniqueUsage.containsKey(Technique.GUESSING) ||
            givens < 17) {
            return Difficulty.INSANE;
        }

        if (givens < 17) return Difficulty.INSANE;
        if (givens < 22) return Difficulty.HARD;
        if (givens < 28) return Difficulty.MEDIUM;
        return Difficulty.EASY;
    }

    private int[][] deepCopy(int[][] board) {
        int[][] copy = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(board[r], 0, copy[r], 0, SIZE);
        }
        return copy;
    }

    public static class DifficultyResult {
        public final int score;
        public final int cost;
        public final Difficulty difficulty;
        public final Map<Technique, Integer> techniqueUsage;
        public final int[][] puzzle;

        public DifficultyResult(int score, Difficulty difficulty, Map<Technique, Integer> techniqueUsage, int[][] puzzle) {
            this.score = score;
            this.cost = score;
            this.difficulty = difficulty;
            this.techniqueUsage = new EnumMap<>(techniqueUsage);
            this.puzzle = deepCopy(puzzle);
        }

        private static int[][] deepCopy(int[][] arr) {
            int[][] copy = new int[arr.length][];
            for (int i = 0; i < arr.length; i++) {
                copy[i] = arr[i].clone();
            }
            return copy;
        }

        @Override
        public String toString() {
            return String.format("Difficulty: %s (score: %d), Techniques: %s", 
                difficulty, score, techniqueUsage);
        }
    }

    private boolean sees(int r1, int c1, int r2, int c2) {
        return r1 == r2 || c1 == c2 || 
               (r1 / BLOCK_SIZE == r2 / BLOCK_SIZE && c1 / BLOCK_SIZE == c2 / BLOCK_SIZE);
    }
}
