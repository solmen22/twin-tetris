package com.example.tetris.game;

import com.example.tetris.domain.TetrominoType;

public final class HoldSlot {

    private TetrominoType heldType;
    private boolean lockedThisPiece;

    public boolean isEmpty() {
        return heldType == null;
    }

    public TetrominoType type() {
        return heldType;
    }

    public boolean canHold() {
        return !lockedThisPiece;
    }

    public TetrominoType swap(TetrominoType incoming) {
        if (lockedThisPiece) {
            throw new IllegalStateException("Hold is locked until the next spawn");
        }
        TetrominoType previous = heldType;
        heldType = incoming;
        lockedThisPiece = true;
        return previous;
    }

    public void unlock() {
        lockedThisPiece = false;
    }

    public void reset() {
        heldType = null;
        lockedThisPiece = false;
    }
}
