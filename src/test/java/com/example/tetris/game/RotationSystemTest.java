package com.example.tetris.game;

import com.example.tetris.domain.Board;
import com.example.tetris.domain.Direction;
import com.example.tetris.domain.Position;
import com.example.tetris.domain.Rotation;
import com.example.tetris.domain.Tetromino;
import com.example.tetris.domain.TetrominoType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RotationSystemTest {

    @Test
    void 空盤面でTミノはSPAWNからRIGHTに回転できる() {
        Board board = new Board();
        Tetromino piece = Tetromino.spawnDown(TetrominoType.T);

        Optional<Tetromino> rotated = RotationSystem.tryRotateCw(board, piece);

        assertThat(rotated).isPresent();
        assertThat(rotated.get().rotation()).isEqualTo(Rotation.RIGHT);
    }

    @Test
    void 空盤面でTミノはCCWでLEFTに回転できる() {
        Board board = new Board();
        Tetromino piece = Tetromino.spawnDown(TetrominoType.T);

        Optional<Tetromino> rotated = RotationSystem.tryRotateCcw(board, piece);

        assertThat(rotated).isPresent();
        assertThat(rotated.get().rotation()).isEqualTo(Rotation.LEFT);
    }

    @Test
    void Oミノの回転は位置を変えない() {
        Board board = new Board();
        Tetromino piece = Tetromino.spawnDown(TetrominoType.O);

        Optional<Tetromino> rotated = RotationSystem.tryRotateCw(board, piece);

        assertThat(rotated).isPresent();
        assertThat(rotated.get().origin()).isEqualTo(piece.origin());
    }

    @Test
    void 左壁に貼り付いたTミノはCCW回転でキックが入って成功する() {
        Board board = new Board();
        // Tミノを左壁ぎりぎりに置く: SPAWN 状態で origin.col = -1 にすると左端の (1,0) のセルが col -1 になり衝突
        // origin.col = 0 にすると (1,0) は col 0、(0,1) は col 1、(1,1) col 1、(1,2) col 2 → 全部 OK
        Tetromino piece = new Tetromino(TetrominoType.T, new Position(5, 0), Rotation.SPAWN, Direction.DOWN);

        Optional<Tetromino> rotated = RotationSystem.tryRotateCcw(board, piece);

        assertThat(rotated).isPresent();
        assertThat(rotated.get().rotation()).isEqualTo(Rotation.LEFT);
    }

    @Test
    void 完全に塞がれていれば回転は失敗する() {
        Board board = new Board();
        Tetromino piece = Tetromino.spawnDown(TetrominoType.T);
        // Tミノの周囲5マス×5マスを埋める
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 8; c++) {
                if (!piece.cells().contains(new Position(r, c)) && r < 10 && c < 10) {
                    board.place(new Position(r, c), TetrominoType.Z);
                }
            }
        }

        Optional<Tetromino> rotated = RotationSystem.tryRotateCw(board, piece);

        assertThat(rotated).isEmpty();
    }

    @Test
    void Iミノは空盤面でSPAWNからRIGHTに回転できる() {
        Board board = new Board();
        Tetromino piece = Tetromino.spawnDown(TetrominoType.I);

        Optional<Tetromino> rotated = RotationSystem.tryRotateCw(board, piece);

        assertThat(rotated).isPresent();
        assertThat(rotated.get().rotation()).isEqualTo(Rotation.RIGHT);
    }
}
