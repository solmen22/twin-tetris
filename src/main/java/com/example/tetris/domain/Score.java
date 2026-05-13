package com.example.tetris.domain;

public record Score(long points, int lines, int level) {

    public static final int INITIAL_LEVEL = 1;
    public static final int LINES_PER_LEVEL = 10;

    public static Score initial() {
        return new Score(0L, 0, INITIAL_LEVEL);
    }

    public Score addPoints(long delta) {
        return new Score(points + delta, lines, level);
    }

    public Score addLines(int delta) {
        int newLines = lines + delta;
        int newLevel = Math.max(INITIAL_LEVEL, INITIAL_LEVEL + newLines / LINES_PER_LEVEL);
        return new Score(points, newLines, newLevel);
    }
}
