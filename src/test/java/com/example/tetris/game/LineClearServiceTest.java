package com.example.tetris.game;

import com.example.tetris.domain.Board;
import com.example.tetris.domain.Cell;
import com.example.tetris.domain.Constants;
import com.example.tetris.domain.Position;
import com.example.tetris.domain.TetrominoType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LineClearServiceTest {

    @Test
    void 空の盤面ではクリアされる行は0() {
        Board board = new Board();

        int cleared = LineClearService.clearUpperHalf(board);

        assertThat(cleared).isZero();
    }

    @Test
    void 上半分の最下行が満たされていれば1ライン消去される() {
        Board board = new Board();
        fillRow(board, Constants.UPPER_FIELD_BOTTOM, TetrominoType.I);

        int cleared = LineClearService.clearUpperHalf(board);

        assertThat(cleared).isOne();
        for (int c = 0; c < Constants.BOARD_WIDTH; c++) {
            assertThat(board.cellAt(Constants.UPPER_FIELD_BOTTOM, c)).isEqualTo(Cell.EMPTY);
        }
    }

    @Test
    void 上半分の2行が同時に満たされていれば2ライン消去される() {
        Board board = new Board();
        fillRow(board, Constants.UPPER_FIELD_BOTTOM - 1, TetrominoType.I);
        fillRow(board, Constants.UPPER_FIELD_BOTTOM, TetrominoType.I);

        int cleared = LineClearService.clearUpperHalf(board);

        assertThat(cleared).isEqualTo(2);
    }

    @Test
    void 上半分の4行同時_テトリス_も消去される() {
        Board board = new Board();
        for (int r = Constants.UPPER_FIELD_BOTTOM - 3; r <= Constants.UPPER_FIELD_BOTTOM; r++) {
            fillRow(board, r, TetrominoType.I);
        }

        int cleared = LineClearService.clearUpperHalf(board);

        assertThat(cleared).isEqualTo(4);
    }

    @Test
    void 一部だけ埋まった行は消去されない() {
        Board board = new Board();
        for (int c = 0; c < Constants.BOARD_WIDTH - 1; c++) {
            board.place(new Position(Constants.UPPER_FIELD_BOTTOM, c), TetrominoType.I);
        }

        int cleared = LineClearService.clearUpperHalf(board);

        assertThat(cleared).isZero();
        assertThat(board.cellAt(Constants.UPPER_FIELD_BOTTOM, 0)).isEqualTo(Cell.I);
    }

    @Test
    void 消去後_上のブロックが下にシフトされる() {
        Board board = new Board();
        // 行 8 に部分的なブロック、行 9 を完全に埋める
        board.place(new Position(Constants.UPPER_FIELD_BOTTOM - 1, 3), TetrominoType.T);
        fillRow(board, Constants.UPPER_FIELD_BOTTOM, TetrominoType.I);

        LineClearService.clearUpperHalf(board);

        assertThat(board.cellAt(Constants.UPPER_FIELD_BOTTOM, 3)).isEqualTo(Cell.T);
        assertThat(board.cellAt(Constants.UPPER_FIELD_BOTTOM - 1, 3)).isEqualTo(Cell.EMPTY);
    }

    @Test
    void 中央境界線_row10_はPhase1では消去されない() {
        Board board = new Board();
        fillRow(board, Constants.CENTER_ROW, TetrominoType.I);

        int cleared = LineClearService.clearUpperHalf(board);

        assertThat(cleared).isZero();
        for (int c = 0; c < Constants.BOARD_WIDTH; c++) {
            assertThat(board.cellAt(Constants.CENTER_ROW, c)).isEqualTo(Cell.I);
        }
    }

    @Test
    void 下半分はPhase1では消去対象外() {
        Board board = new Board();
        fillRow(board, Constants.LOWER_FIELD_TOP, TetrominoType.I);

        int cleared = LineClearService.clearUpperHalf(board);

        assertThat(cleared).isZero();
    }

    private static void fillRow(Board board, int row, TetrominoType type) {
        for (int c = 0; c < Constants.BOARD_WIDTH; c++) {
            board.place(new Position(row, c), type);
        }
    }
}
