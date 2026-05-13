package com.example.tetris.game;

import com.example.tetris.domain.Board;
import com.example.tetris.domain.Rotation;
import com.example.tetris.domain.Tetromino;

import java.util.Optional;

public final class RotationSystem {

    private RotationSystem() {
    }

    public static Optional<Tetromino> tryRotateCw(Board board, Tetromino piece) {
        return tryRotate(board, piece, piece.rotation().rotateCw());
    }

    public static Optional<Tetromino> tryRotateCcw(Board board, Tetromino piece) {
        return tryRotate(board, piece, piece.rotation().rotateCcw());
    }

    private static Optional<Tetromino> tryRotate(Board board, Tetromino piece, Rotation target) {
        Tetromino rotated = piece.withRotation(target);
        int[][] kicks = WallKicks.offsetsFor(piece.direction(), piece.type(), piece.rotation(), target);
        for (int[] kick : kicks) {
            Tetromino candidate = rotated.translated(kick[0], kick[1]);
            if (!CollisionDetector.collides(board, candidate)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }
}
