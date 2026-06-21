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
        // 後方互換: 向きから半面を導出(通常のミノは向き=半面)。
        return collides(board, piece, piece.direction() == Direction.UP);
    }

    /**
     * ミノが指定の半面に留まる前提で衝突判定する。
     * @param lowerHalf true なら下半面(中央より上へ行けない)、false なら上半面(中央より下へ行けない)。
     */
    public static boolean collides(Board board, Tetromino piece, boolean lowerHalf) {
        for (Position p : piece.cells()) {
            if (!board.isInside(p)) {
                return true;
            }
            if (!board.cellAt(p).isEmpty()) {
                return true;
            }
            if (crossesCenterBoundary(lowerHalf, p)) {
                return true;
            }
        }
        return false;
    }

    private static boolean crossesCenterBoundary(boolean lowerHalf, Position p) {
        if (lowerHalf) {
            return p.row() < Constants.CENTER_ROW;   // 下半面のミノは中央より上へ行けない
        }
        return p.row() > Constants.CENTER_ROW;        // 上半面のミノは中央より下へ行けない
    }
}
