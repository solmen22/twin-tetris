package com.example.tetris.game;

import com.example.tetris.domain.Rotation;
import com.example.tetris.domain.TetrominoType;

import java.util.EnumMap;
import java.util.Map;

public final class WallKicks {

    private record Transition(Rotation from, Rotation to) {
    }

    private static final Map<Transition, int[][]> JLSTZ_KICKS = new java.util.HashMap<>();
    private static final Map<Transition, int[][]> I_KICKS = new java.util.HashMap<>();

    static {
        // J/L/S/T/Z (drow, dcol) — row-down coordinate
        JLSTZ_KICKS.put(new Transition(Rotation.SPAWN, Rotation.RIGHT),
            new int[][] {{0, 0}, {0, -1}, {-1, -1}, {2, 0}, {2, -1}});
        JLSTZ_KICKS.put(new Transition(Rotation.RIGHT, Rotation.SPAWN),
            new int[][] {{0, 0}, {0, 1}, {1, 1}, {-2, 0}, {-2, 1}});
        JLSTZ_KICKS.put(new Transition(Rotation.RIGHT, Rotation.HALF),
            new int[][] {{0, 0}, {0, 1}, {1, 1}, {-2, 0}, {-2, 1}});
        JLSTZ_KICKS.put(new Transition(Rotation.HALF, Rotation.RIGHT),
            new int[][] {{0, 0}, {0, -1}, {-1, -1}, {2, 0}, {2, -1}});
        JLSTZ_KICKS.put(new Transition(Rotation.HALF, Rotation.LEFT),
            new int[][] {{0, 0}, {0, 1}, {-1, 1}, {2, 0}, {2, 1}});
        JLSTZ_KICKS.put(new Transition(Rotation.LEFT, Rotation.HALF),
            new int[][] {{0, 0}, {0, -1}, {1, -1}, {-2, 0}, {-2, -1}});
        JLSTZ_KICKS.put(new Transition(Rotation.LEFT, Rotation.SPAWN),
            new int[][] {{0, 0}, {0, -1}, {1, -1}, {-2, 0}, {-2, -1}});
        JLSTZ_KICKS.put(new Transition(Rotation.SPAWN, Rotation.LEFT),
            new int[][] {{0, 0}, {0, 1}, {-1, 1}, {2, 0}, {2, 1}});

        // I (drow, dcol) — row-down coordinate
        I_KICKS.put(new Transition(Rotation.SPAWN, Rotation.RIGHT),
            new int[][] {{0, 0}, {0, -2}, {0, 1}, {1, -2}, {-2, 1}});
        I_KICKS.put(new Transition(Rotation.RIGHT, Rotation.SPAWN),
            new int[][] {{0, 0}, {0, 2}, {0, -1}, {-1, 2}, {2, -1}});
        I_KICKS.put(new Transition(Rotation.RIGHT, Rotation.HALF),
            new int[][] {{0, 0}, {0, -1}, {0, 2}, {-2, -1}, {1, 2}});
        I_KICKS.put(new Transition(Rotation.HALF, Rotation.RIGHT),
            new int[][] {{0, 0}, {0, 1}, {0, -2}, {2, 1}, {-1, -2}});
        I_KICKS.put(new Transition(Rotation.HALF, Rotation.LEFT),
            new int[][] {{0, 0}, {0, 2}, {0, -1}, {-1, 2}, {2, -1}});
        I_KICKS.put(new Transition(Rotation.LEFT, Rotation.HALF),
            new int[][] {{0, 0}, {0, -2}, {0, 1}, {1, -2}, {-2, 1}});
        I_KICKS.put(new Transition(Rotation.LEFT, Rotation.SPAWN),
            new int[][] {{0, 0}, {0, 1}, {0, -2}, {2, 1}, {-1, -2}});
        I_KICKS.put(new Transition(Rotation.SPAWN, Rotation.LEFT),
            new int[][] {{0, 0}, {0, -1}, {0, 2}, {-2, -1}, {1, 2}});
    }

    private static final int[][] NO_KICK = new int[][] {{0, 0}};

    private WallKicks() {
    }

    public static int[][] offsetsForDown(TetrominoType type, Rotation from, Rotation to) {
        if (type == TetrominoType.O) {
            return NO_KICK;
        }
        Transition key = new Transition(from, to);
        if (type == TetrominoType.I) {
            return I_KICKS.getOrDefault(key, NO_KICK);
        }
        return JLSTZ_KICKS.getOrDefault(key, NO_KICK);
    }
}
