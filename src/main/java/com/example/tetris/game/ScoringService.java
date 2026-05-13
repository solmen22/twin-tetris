package com.example.tetris.game;

public final class ScoringService {

    private static final int[] LINE_CLEAR_BASE = {0, 100, 300, 500, 800};
    private static final int HARD_DROP_POINTS_PER_CELL = 2;
    private static final int SOFT_DROP_POINTS_PER_CELL = 1;
    private static final int MAX_LINES_PER_LOCK = 4;

    private ScoringService() {
    }

    public static long lineClearPoints(int linesCleared, int level) {
        if (linesCleared <= 0 || linesCleared > MAX_LINES_PER_LOCK) {
            return 0L;
        }
        return (long) LINE_CLEAR_BASE[linesCleared] * Math.max(1, level);
    }

    public static long hardDropPoints(int cellsDropped) {
        if (cellsDropped <= 0) {
            return 0L;
        }
        return (long) cellsDropped * HARD_DROP_POINTS_PER_CELL;
    }

    public static long softDropPoints(int cellsDropped) {
        if (cellsDropped <= 0) {
            return 0L;
        }
        return (long) cellsDropped * SOFT_DROP_POINTS_PER_CELL;
    }
}
