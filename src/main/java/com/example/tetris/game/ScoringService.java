package com.example.tetris.game;

import com.example.tetris.game.LineClearService.CascadeStep;
import com.example.tetris.game.LineClearService.LineClearResult;

public final class ScoringService {

    private static final int[] LINE_CLEAR_BASE = {0, 100, 300, 500, 800};
    private static final int EXTRA_LINE_INCREMENT = 200;
    private static final int TETRIS_LINES = 4;

    private static final int CENTER_BOUNDARY_MULTIPLIER = 2;
    private static final int SIMULTANEOUS_MULTIPLIER = 5;
    private static final int REN_BONUS_PER_CHAIN = 50;

    // SPEC 9.2: Back-to-Back Tetris は連続テトリス(4 ライン消去)に 1.5 倍。
    // 端数は四捨五入で決定的に丸める。
    private static final double BACK_TO_BACK_MULTIPLIER = 1.5;

    private static final int HARD_DROP_POINTS_PER_CELL = 2;
    private static final int SOFT_DROP_POINTS_PER_CELL = 1;

    private ScoringService() {
    }

    public static long lineClearPoints(int linesCleared, int level) {
        if (linesCleared <= 0) {
            return 0L;
        }
        int safeLevel = Math.max(1, level);
        if (linesCleared <= TETRIS_LINES) {
            return (long) LINE_CLEAR_BASE[linesCleared] * safeLevel;
        }
        long extended = LINE_CLEAR_BASE[TETRIS_LINES]
            + (long) EXTRA_LINE_INCREMENT * (linesCleared - TETRIS_LINES);
        return extended * safeLevel;
    }

    public static long stepPoints(CascadeStep step, int level) {
        if (step == null || step.isEmpty()) {
            return 0L;
        }
        long base = lineClearPoints(step.totalLines(), level);
        return base * multiplierFor(step);
    }

    public static int multiplierFor(CascadeStep step) {
        if (step.isSimultaneous()) {
            return SIMULTANEOUS_MULTIPLIER;
        }
        if (step.centerCleared()) {
            return CENTER_BOUNDARY_MULTIPLIER;
        }
        return 1;
    }

    public static long chainBonus(int chainCount) {
        if (chainCount <= 0) {
            return 0L;
        }
        return (long) chainCount * REN_BONUS_PER_CHAIN;
    }

    public static long resultPoints(LineClearResult result, int level) {
        return resultPoints(result, level, false);
    }

    /**
     * Back-to-Back ボーナスを考慮してロック 1 回分の合計得点を算出する。
     * {@code backToBack} が true のとき、テトリス(4 ライン以上)を含むステップに
     * {@link #BACK_TO_BACK_MULTIPLIER} を掛ける(SPEC 9.2)。
     */
    public static long resultPoints(LineClearResult result, int level, boolean backToBack) {
        long total = 0L;
        for (CascadeStep step : result.steps()) {
            long stepScore = stepPoints(step, level);
            if (backToBack && step.totalLines() >= TETRIS_LINES) {
                stepScore = Math.round(stepScore * BACK_TO_BACK_MULTIPLIER);
            }
            total += stepScore;
        }
        total += chainBonus(result.chainCount());
        return total;
    }

    /** ロック 1 回の結果がテトリス(単一ステップで 4 ライン以上消去)を含むか。Back-to-Back 判定に使う。 */
    public static boolean isTetrisResult(LineClearResult result) {
        for (CascadeStep step : result.steps()) {
            if (step.totalLines() >= TETRIS_LINES) {
                return true;
            }
        }
        return false;
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
