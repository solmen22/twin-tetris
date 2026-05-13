package com.example.tetris.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoardTest {

    @Test
    void 新規ボードはSPECの幅と高さを持つ() {
        Board board = new Board();

        assertThat(board.width()).isEqualTo(Constants.BOARD_WIDTH);
        assertThat(board.height()).isEqualTo(Constants.BOARD_HEIGHT);
    }

    @Test
    void 新規ボードのすべてのセルはEMPTY() {
        Board board = new Board();

        for (int r = 0; r < board.height(); r++) {
            for (int c = 0; c < board.width(); c++) {
                assertThat(board.cellAt(r, c)).as("(%d,%d)", r, c).isEqualTo(Cell.EMPTY);
            }
        }
    }

    @Test
    void isInsideは盤面内をtrue盤面外をfalseと判定する() {
        Board board = new Board();

        assertThat(board.isInside(new Position(0, 0))).isTrue();
        assertThat(board.isInside(new Position(20, 9))).isTrue();
        assertThat(board.isInside(new Position(-1, 0))).isFalse();
        assertThat(board.isInside(new Position(0, -1))).isFalse();
        assertThat(board.isInside(new Position(21, 0))).isFalse();
        assertThat(board.isInside(new Position(0, 10))).isFalse();
    }

    @Test
    void placeで指定したセルが該当ミノタイプで埋まる() {
        Board board = new Board();

        board.place(new Position(5, 3), TetrominoType.T);

        assertThat(board.cellAt(5, 3)).isEqualTo(Cell.T);
        assertThat(board.cellAt(5, 3).type()).isEqualTo(TetrominoType.T);
        assertThat(board.isEmpty(5, 3)).isFalse();
    }

    @Test
    void place_盤面外は例外() {
        Board board = new Board();

        assertThatThrownBy(() -> board.place(new Position(-1, 0), TetrominoType.T))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isRowFullは完全に埋まった行のみtrue() {
        Board board = new Board();

        for (int c = 0; c < board.width(); c++) {
            board.place(new Position(5, c), TetrominoType.I);
        }

        assertThat(board.isRowFull(5)).isTrue();
        assertThat(board.isRowFull(4)).isFalse();
    }

    @Test
    void 一部だけ埋まった行はisRowFullがfalse() {
        Board board = new Board();

        for (int c = 0; c < board.width() - 1; c++) {
            board.place(new Position(5, c), TetrominoType.I);
        }

        assertThat(board.isRowFull(5)).isFalse();
    }

    @Test
    void clearRowは指定行を全てEMPTYに戻す() {
        Board board = new Board();
        for (int c = 0; c < board.width(); c++) {
            board.place(new Position(5, c), TetrominoType.I);
        }

        board.clearRow(5);

        for (int c = 0; c < board.width(); c++) {
            assertThat(board.isEmpty(5, c)).isTrue();
        }
    }

    @Test
    void copyは独立したインスタンスを返す() {
        Board board = new Board();
        board.place(new Position(5, 3), TetrominoType.T);

        Board copy = board.copy();
        copy.place(new Position(5, 4), TetrominoType.Z);

        assertThat(board.isEmpty(5, 4)).isTrue();
        assertThat(copy.cellAt(5, 3)).isEqualTo(Cell.T);
        assertThat(copy.cellAt(5, 4)).isEqualTo(Cell.Z);
    }

    @Test
    void setRowで行全体を一括上書きできる() {
        Board board = new Board();

        java.util.List<Cell> row = new java.util.ArrayList<>();
        for (int c = 0; c < board.width(); c++) {
            row.add(Cell.I);
        }
        board.setRow(7, row);

        for (int c = 0; c < board.width(); c++) {
            assertThat(board.cellAt(7, c)).isEqualTo(Cell.I);
        }
    }
}
