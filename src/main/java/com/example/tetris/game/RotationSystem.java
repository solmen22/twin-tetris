package com.example.tetris.game;

import com.example.tetris.domain.Board;
import com.example.tetris.domain.Direction;
import com.example.tetris.domain.Rotation;
import com.example.tetris.domain.Tetromino;

import java.util.Optional;

public final class RotationSystem {

    private RotationSystem() {
    }

    public static Optional<Tetromino> tryRotateCw(Board board, Tetromino piece) {
        return tryRotateCw(board, piece, piece.direction() == Direction.UP);
    }

    public static Optional<Tetromino> tryRotateCcw(Board board, Tetromino piece) {
        return tryRotateCcw(board, piece, piece.direction() == Direction.UP);
    }

    public static Optional<Tetromino> tryRotateCw(Board board, Tetromino piece, boolean lowerHalf) {
        return tryRotate(board, piece, piece.rotation().rotateCw(), lowerHalf);
    }

    public static Optional<Tetromino> tryRotateCcw(Board board, Tetromino piece, boolean lowerHalf) {
        return tryRotate(board, piece, piece.rotation().rotateCcw(), lowerHalf);
    }

    private static Optional<Tetromino> tryRotate(Board board, Tetromino piece, Rotation target, boolean lowerHalf) {
        Tetromino rotated = piece.withRotation(target);
        // 壁蹴りのオフセットは重力方向(direction)で上下反転する。中央境界は半面で判定する。
        int[][] kicks = WallKicks.offsetsFor(piece.direction(), piece.type(), piece.rotation(), target);
        for (int[] kick : kicks) {
            Tetromino candidate = rotated.translated(kick[0], kick[1]);
            if (!CollisionDetector.collides(board, candidate, lowerHalf)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }
}
