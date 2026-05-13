package com.example.tetris.domain;

public enum Rotation {
    SPAWN,
    RIGHT,
    HALF,
    LEFT;

    public Rotation rotateCw() {
        return switch (this) {
            case SPAWN -> RIGHT;
            case RIGHT -> HALF;
            case HALF -> LEFT;
            case LEFT -> SPAWN;
        };
    }

    public Rotation rotateCcw() {
        return switch (this) {
            case SPAWN -> LEFT;
            case RIGHT -> SPAWN;
            case HALF -> RIGHT;
            case LEFT -> HALF;
        };
    }
}
