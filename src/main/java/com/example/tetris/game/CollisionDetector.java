package com.example.tetris.game;

import com.example.tetris.domain.Board;
import com.example.tetris.domain.Constants;
import com.example.tetris.domain.Direction;
import com.example.tetris.domain.Position;
import com.example.tetris.domain.Tetromino;

public final class CollisionDetector {

    private CollisionDetector() {
    }

    public static boolean collides(Board board, Tetromino piece) {
        for (Position p : piece.cells()) {
            if (!board.isInside(p)) {
                return true;
            }
            if (!board.cellAt(p).isEmpty()) {
                return true;
            }
            if (crossesCenterBoundary(piece.direction(), p)) {
                return true;
            }
        }
        return false;
    }

    private static boolean crossesCenterBoundary(Direction direction, Position p) {
        if (direction == Direction.DOWN) {
            return p.row() > Constants.CENTER_ROW;
        }
        return p.row() < Constants.CENTER_ROW;
    }
}
