package com.example.tetris.domain;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public enum TetrominoType {
    I(0, 240, 240),
    O(240, 240, 0),
    T(160, 0, 240),
    S(0, 240, 0),
    Z(240, 0, 0),
    J(0, 0, 240),
    L(240, 160, 0);

    private final int red;
    private final int green;
    private final int blue;
    private final Map<Rotation, List<Position>> cellsByRotation;

    TetrominoType(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.cellsByRotation = new EnumMap<>(Rotation.class);
    }

    public int red() {
        return red;
    }

    public int green() {
        return green;
    }

    public int blue() {
        return blue;
    }

    public List<Position> cellsAt(Rotation rotation) {
        return cellsByRotation.get(rotation);
    }

    public int boundingBoxSize() {
        return this == I ? 4 : 3;
    }

    private void register(Rotation rotation, int[][] cells) {
        List<Position> positions = new java.util.ArrayList<>(cells.length);
        for (int[] rc : cells) {
            positions.add(new Position(rc[0], rc[1]));
        }
        cellsByRotation.put(rotation, List.copyOf(positions));
    }

    static {
        I.register(Rotation.SPAWN, new int[][] {{1, 0}, {1, 1}, {1, 2}, {1, 3}});
        I.register(Rotation.RIGHT, new int[][] {{0, 2}, {1, 2}, {2, 2}, {3, 2}});
        I.register(Rotation.HALF,  new int[][] {{2, 0}, {2, 1}, {2, 2}, {2, 3}});
        I.register(Rotation.LEFT,  new int[][] {{0, 1}, {1, 1}, {2, 1}, {3, 1}});

        for (Rotation r : Rotation.values()) {
            O.register(r, new int[][] {{0, 1}, {0, 2}, {1, 1}, {1, 2}});
        }

        T.register(Rotation.SPAWN, new int[][] {{0, 1}, {1, 0}, {1, 1}, {1, 2}});
        T.register(Rotation.RIGHT, new int[][] {{0, 1}, {1, 1}, {1, 2}, {2, 1}});
        T.register(Rotation.HALF,  new int[][] {{1, 0}, {1, 1}, {1, 2}, {2, 1}});
        T.register(Rotation.LEFT,  new int[][] {{0, 1}, {1, 0}, {1, 1}, {2, 1}});

        S.register(Rotation.SPAWN, new int[][] {{0, 1}, {0, 2}, {1, 0}, {1, 1}});
        S.register(Rotation.RIGHT, new int[][] {{0, 1}, {1, 1}, {1, 2}, {2, 2}});
        S.register(Rotation.HALF,  new int[][] {{1, 1}, {1, 2}, {2, 0}, {2, 1}});
        S.register(Rotation.LEFT,  new int[][] {{0, 0}, {1, 0}, {1, 1}, {2, 1}});

        Z.register(Rotation.SPAWN, new int[][] {{0, 0}, {0, 1}, {1, 1}, {1, 2}});
        Z.register(Rotation.RIGHT, new int[][] {{0, 2}, {1, 1}, {1, 2}, {2, 1}});
        Z.register(Rotation.HALF,  new int[][] {{1, 0}, {1, 1}, {2, 1}, {2, 2}});
        Z.register(Rotation.LEFT,  new int[][] {{0, 1}, {1, 0}, {1, 1}, {2, 0}});

        J.register(Rotation.SPAWN, new int[][] {{0, 0}, {1, 0}, {1, 1}, {1, 2}});
        J.register(Rotation.RIGHT, new int[][] {{0, 1}, {0, 2}, {1, 1}, {2, 1}});
        J.register(Rotation.HALF,  new int[][] {{1, 0}, {1, 1}, {1, 2}, {2, 2}});
        J.register(Rotation.LEFT,  new int[][] {{0, 1}, {1, 1}, {2, 0}, {2, 1}});

        L.register(Rotation.SPAWN, new int[][] {{0, 2}, {1, 0}, {1, 1}, {1, 2}});
        L.register(Rotation.RIGHT, new int[][] {{0, 1}, {1, 1}, {2, 1}, {2, 2}});
        L.register(Rotation.HALF,  new int[][] {{1, 0}, {1, 1}, {1, 2}, {2, 0}});
        L.register(Rotation.LEFT,  new int[][] {{0, 0}, {0, 1}, {1, 1}, {2, 1}});
    }
}
