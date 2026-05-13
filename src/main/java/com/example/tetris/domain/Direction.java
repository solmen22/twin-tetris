package com.example.tetris.domain;

public enum Direction {
    DOWN(1),
    UP(-1);

    private final int rowStep;

    Direction(int rowStep) {
        this.rowStep = rowStep;
    }

    public int rowStep() {
        return rowStep;
    }

    public Direction opposite() {
        return this == DOWN ? UP : DOWN;
    }
}
