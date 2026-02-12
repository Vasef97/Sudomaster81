package com.ltp.sudomaster.sudokugen;

public class Cell {
    public int r;
    public int c;

    public Cell(int r, int c) {
        this.r = r;
        this.c = c;
    }

    @Override
    public String toString() {
        return "(" + r + "," + c + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell)) return false;
        Cell cell = (Cell) o;
        return r == cell.r && c == cell.c;
    }

    @Override
    public int hashCode() {
        return 31 * r + c;
    }
}
