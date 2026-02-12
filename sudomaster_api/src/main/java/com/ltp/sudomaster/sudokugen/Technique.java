package com.ltp.sudomaster.sudokugen;

public enum Technique {
    SINGLE_CANDIDATE(1),
    SINGLE_POSITION(1),
    NAKED_PAIR(4),
    HIDDEN_PAIR(6),
    
    POINTING_PAIR(7),
    BOX_LINE(7),
    NAKED_TRIPLE(8),
    HIDDEN_TRIPLE(10),
    
    X_WING(18),
    XY_WING(22),
    COLORING(30),
    
    SWORDFISH(35),
    ADVANCED_COLORING(40),
    FORCING_CHAIN(65),
    GUESSING(160);

    private final int weight;

    Technique(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
