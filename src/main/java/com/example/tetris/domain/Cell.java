package com.example.tetris.domain;

public enum Cell {
    EMPTY(null),
    I(TetrominoType.I),
    O(TetrominoType.O),
    T(TetrominoType.T),
    S(TetrominoType.S),
    Z(TetrominoType.Z),
    J(TetrominoType.J),
    L(TetrominoType.L);

    private final TetrominoType type;

    Cell(TetrominoType type) {
        this.type = type;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public boolean isFilled() {
        return this != EMPTY;
    }

    public TetrominoType type() {
        return type;
    }

    public static Cell of(TetrominoType type) {
        if (type == null) {
            return EMPTY;
        }
        return switch (type) {
            case I -> I;
            case O -> O;
            case T -> T;
            case S -> S;
            case Z -> Z;
            case J -> J;
            case L -> L;
        };
    }
}
