package com.example.tetris.game;

import com.example.tetris.domain.Board;
import com.example.tetris.domain.Constants;
import com.example.tetris.domain.Direction;
import com.example.tetris.domain.Position;
import com.example.tetris.domain.Rotation;
import com.example.tetris.domain.Tetromino;
import com.example.tetris.domain.TetrominoType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CollisionDetectorTest {

    @Test
    void 空の盤面内に置かれたミノは衝突しない() {
        Board board = new Board();
        Tetromino piece = Tetromino.spawnDown(TetrominoType.T);

        assertThat(CollisionDetector.collides(board, piece)).isFalse();
    }

    @Test
    void 盤面左端を越えたミノは衝突する() {
        Board board = new Board();
        Tetromino piece = new Tetromino(TetrominoType.T, new Position(5, -1), Rotation.SPAWN, Direction.DOWN);

        assertThat(CollisionDetector.collides(board, piece)).isTrue();
    }

    @Test
    void 盤面右端を越えたミノは衝突する() {
        Board board = new Board();
        Tetromino piece = new Tetromino(TetrominoType.T, new Position(5, Constants.BOARD_WIDTH - 2), Rotation.SPAWN, Direction.DOWN);

        assertThat(CollisionDetector.collides(board, piece)).isTrue();
    }

    @Test
    void DOWNミノは中央境界線_row10_までは許容される() {
        Board board = new Board();
        Tetromino piece = new Tetromino(TetrominoType.O, new Position(Constants.CENTER_ROW - 1, 4), Rotation.SPAWN, Direction.DOWN);

        assertThat(CollisionDetector.collides(board, piece)).isFalse();
    }

    @Test
    void DOWNミノはrow11以下に侵入できない() {
        Board board = new Board();
        Tetromino piece = new Tetromino(TetrominoType.O, new Position(Constants.CENTER_ROW, 4), Rotation.SPAWN, Direction.DOWN);

        assertThat(CollisionDetector.collides(board, piece)).isTrue();
    }

    @Test
    void UPミノはrow10までは許容される() {
        Board board = new Board();
        Tetromino piece = new Tetromino(TetrominoType.O, new Position(Constants.CENTER_ROW, 4), Rotation.SPAWN, Direction.UP);

        assertThat(CollisionDetector.collides(board, piece)).isFalse();
    }

    @Test
    void UPミノはrow9以上に侵入できない() {
        Board board = new Board();
        Tetromino piece = new Tetromino(TetrominoType.O, new Position(Constants.CENTER_ROW - 2, 4), Rotation.SPAWN, Direction.UP);

        assertThat(CollisionDetector.collides(board, piece)).isTrue();
    }

    @Test
    void 既存ブロックと重なるミノは衝突する() {
        Board board = new Board();
        for (Position p : Tetromino.spawnDown(TetrominoType.T).cells()) {
            board.place(p, TetrominoType.Z);
        }

        Tetromino piece = Tetromino.spawnDown(TetrominoType.T);

        assertThat(CollisionDetector.collides(board, piece)).isTrue();
    }
}
