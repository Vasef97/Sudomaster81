package com.ltp.sudomaster.sudokugen;

import java.util.Set;

public class DifficultyProfile {
    public final int minGivens;
    public final int maxGivens;
    
    public final int minCost;
    public final int maxCost;
    
    public final Set<Technique> allowedTechniques;
    
    public final boolean forbidGuessing;
    public final boolean forbidAdvanced;
    public final boolean forbidSwordfish;
    public final boolean forbidAdvancedColoring;
    public final boolean forbidForcingChain;

    private DifficultyProfile(Builder builder) {
        this.minGivens = builder.minGivens;
        this.maxGivens = builder.maxGivens;
        this.minCost = builder.minCost;
        this.maxCost = builder.maxCost;
        this.allowedTechniques = builder.allowedTechniques;
        this.forbidGuessing = builder.forbidGuessing;
        this.forbidAdvanced = builder.forbidAdvanced;
        this.forbidSwordfish = builder.forbidSwordfish;
        this.forbidAdvancedColoring = builder.forbidAdvancedColoring;
        this.forbidForcingChain = builder.forbidForcingChain;
    }

    public static class Builder {
        private int minGivens;
        private int maxGivens;
        private int minCost;
        private int maxCost;
        private Set<Technique> allowedTechniques;
        private boolean forbidGuessing = false;
        private boolean forbidAdvanced = false;
        private boolean forbidSwordfish = false;
        private boolean forbidAdvancedColoring = false;
        private boolean forbidForcingChain = false;

        public Builder minGivens(int minGivens) {
            this.minGivens = minGivens;
            return this;
        }

        public Builder maxGivens(int maxGivens) {
            this.maxGivens = maxGivens;
            return this;
        }

        public Builder minCost(int minCost) {
            this.minCost = minCost;
            return this;
        }

        public Builder maxCost(int maxCost) {
            this.maxCost = maxCost;
            return this;
        }

        public Builder allowedTechniques(Set<Technique> techniques) {
            this.allowedTechniques = techniques;
            return this;
        }

        public Builder forbidGuessing(boolean forbid) {
            this.forbidGuessing = forbid;
            return this;
        }

        public Builder forbidAdvanced(boolean forbid) {
            this.forbidAdvanced = forbid;
            return this;
        }

        public Builder forbidSwordfish(boolean forbid) {
            this.forbidSwordfish = forbid;
            return this;
        }

        public Builder forbidAdvancedColoring(boolean forbid) {
            this.forbidAdvancedColoring = forbid;
            return this;
        }

        public Builder forbidForcingChain(boolean forbid) {
            this.forbidForcingChain = forbid;
            return this;
        }

        public DifficultyProfile build() {
            return new DifficultyProfile(this);
        }
    }

    @Override
    public String toString() {
        return "DifficultyProfile{" +
                "givens=" + minGivens + "-" + maxGivens +
                ", cost=" + minCost + "-" + maxCost +
                ", techniques=" + allowedTechniques +
                '}';
    }
}
