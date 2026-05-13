package com.example.tetris.game;

import com.example.tetris.domain.Board;
import com.example.tetris.domain.Cell;
import com.example.tetris.domain.Constants;
import com.example.tetris.domain.Position;
import com.example.tetris.domain.TetrominoType;
import com.example.tetris.game.LineClearService.LineClearResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LineClearServiceTest {

    @Test
    void 空の盤面では結果が空() {
        Board board = new Board();

        LineClearResult result = LineClearService.processClears(board);

        assertThat(result.isEmpty()).isTrue();
        assertThat(result.totalLines()).isZero();
        assertThat(result.chainCount()).isZero();
    }

    @Test
    void 上半分の最下行が満たされていれば1ライン消去される() {
        Board board = new Board();
        fillRow(board, Constants.UPPER_FIELD_BOTTOM, TetrominoType.I);

        LineClearResult result = LineClearService.processClears(board);

        assertThat(result.totalLines()).isOne();
        assertThat(result.steps()).hasSize(1);
        assertThat(result.steps().get(0).upperLines()).isOne();
        assertThat(result.steps().get(0).centerCleared()).isFalse();
    }

    @Test
    void 上半分の4ライン同時消去はTetrisとして1ステップ() {
        Board board = new Board();
        for (int r = Constants.UPPER_FIELD_BOTTOM - 3; r <= Constants.UPPER_FIELD_BOTTOM; r++) {
            fillRow(board, r, TetrominoType.I);
        }

        LineClearResult result = LineClearService.processClears(board);

        assertThat(result.totalLines()).isEqualTo(4);
        assertThat(result.steps()).hasSize(1);
        assertThat(result.steps().get(0).upperLines()).isEqualTo(4);
    }

    @Test
    void 下半分の最上行が満たされていれば1ライン消去される() {
        Board board = new Board();
        fillRow(board, Constants.LOWER_FIELD_TOP, TetrominoType.I);

        LineClearResult result = LineClearService.processClears(board);

        assertThat(result.totalLines()).isOne();
        assertThat(result.steps().get(0).lowerLines()).isOne();
    }

    @Test
    void 下半分の2行同時消去でカスケード方向は中央寄せ() {
        Board board = new Board();
        // row 12 を完全に埋める、row 11 の col 5 だけ T で埋めて穴を残す
        board.place(new Position(Constants.LOWER_FIELD_TOP, 5), TetrominoType.T);
        fillRow(board, Constants.LOWER_FIELD_TOP + 1, TetrominoType.I);
        fillRow(board, Constants.LOWER_FIELD_TOP + 2, TetrominoType.I);

        LineClearResult result = LineClearService.processClears(board);

        assertThat(result.totalLines()).isEqualTo(2);
        // 残った T は中央方向(上)に詰められて row 11 に来る
        assertThat(board.cellAt(Constants.LOWER_FIELD_TOP, 5)).isEqualTo(Cell.T);
    }

    @Test
    void 中央境界線_row10_のみ満杯なら1ライン消去() {
        Board board = new Board();
        fillRow(board, Constants.CENTER_ROW, TetrominoType.I);

        LineClearResult result = LineClearService.processClears(board);

        assertThat(result.totalLines()).isOne();
        assertThat(result.steps().get(0).centerCleared()).isTrue();
        for (int c = 0; c < Constants.BOARD_WIDTH; c++) {
            assertThat(board.cellAt(Constants.CENTER_ROW, c)).isEqualTo(Cell.EMPTY);
        }
    }

    @Test
    void 中央境界線消去後_上半分が1行下にシフト() {
        Board board = new Board();
        // 上半分の row 9 に T を置く
        board.place(new Position(Constants.UPPER_FIELD_BOTTOM, 3), TetrominoType.T);
        // 中央境界線を埋める
        fillRow(board, Constants.CENTER_ROW, TetrominoType.I);

        LineClearService.processClears(board);

        // T は row 9 → row 10 に移動
        assertThat(board.cellAt(Constants.UPPER_FIELD_BOTTOM, 3)).isEqualTo(Cell.EMPTY);
        assertThat(board.cellAt(Constants.CENTER_ROW, 3)).isEqualTo(Cell.T);
    }

    @Test
    void 中央境界線消去後_下半分が1行上にシフト() {
        Board board = new Board();
        // 下半分の row 11 に Z を置く
        board.place(new Position(Constants.LOWER_FIELD_TOP, 6), TetrominoType.Z);
        // 中央境界線を埋める
        fillRow(board, Constants.CENTER_ROW, TetrominoType.I);

        LineClearService.processClears(board);

        // Z は row 11 → row 10 に移動
        assertThat(board.cellAt(Constants.LOWER_FIELD_TOP, 6)).isEqualTo(Cell.EMPTY);
        assertThat(board.cellAt(Constants.CENTER_ROW, 6)).isEqualTo(Cell.Z);
    }

    @Test
    void 上下が同時に揃った場合_カスケードで連鎖が起きる() {
        Board board = new Board();
        // row 9 と row 11 を満杯にし、row 8 と row 12 にも T を点在配置して連鎖が起きるよう仕込む
        fillRow(board, Constants.UPPER_FIELD_BOTTOM, TetrominoType.I);
        fillRow(board, Constants.LOWER_FIELD_TOP, TetrominoType.I);
        fillRow(board, Constants.CENTER_ROW, TetrominoType.I);

        LineClearResult result = LineClearService.processClears(board);

        // 最初のステップで row 9, row 10, row 11 が消えて 3 ライン
        assertThat(result.totalLines()).isGreaterThanOrEqualTo(3);
        assertThat(result.steps().get(0).isSimultaneous()).isTrue();
    }

    @Test
    void 中央境界線消去後の上下噛み合いで連鎖が発火する() {
        Board board = new Board();
        // row 9 col 0-4 を T で半分埋め(上半分の最下行)
        for (int c = 0; c <= 4; c++) {
            board.place(new Position(Constants.UPPER_FIELD_BOTTOM, c), TetrominoType.T);
        }
        // row 11 col 5-9 を Z で半分埋め(下半分の最上行)
        for (int c = 5; c <= 9; c++) {
            board.place(new Position(Constants.LOWER_FIELD_TOP, c), TetrominoType.Z);
        }
        // row 10 完全埋め
        fillRow(board, Constants.CENTER_ROW, TetrominoType.I);

        LineClearResult result = LineClearService.processClears(board);

        // step 1: 中央消去 + 上下シフト → row 10 が T+Z の合体で再満杯
        // step 2: 再び中央消去 → row 10 空に → 終了
        assertThat(result.steps()).hasSize(2);
        assertThat(result.chainCount()).isOne();
        assertThat(result.steps().get(0).centerCleared()).isTrue();
        assertThat(result.steps().get(1).centerCleared()).isTrue();
    }

    @Test
    void 部分的に埋まった行は消去されない() {
        Board board = new Board();
        for (int c = 0; c < Constants.BOARD_WIDTH - 1; c++) {
            board.place(new Position(Constants.UPPER_FIELD_BOTTOM, c), TetrominoType.I);
        }

        LineClearResult result = LineClearService.processClears(board);

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void 中央境界線消去で上下のセルが噛み合って衝突しない場合_両方残る() {
        Board board = new Board();
        // row 9 col 0-2 を T、row 11 col 7-9 を Z で配置(列が重ならない)
        for (int c = 0; c <= 2; c++) {
            board.place(new Position(Constants.UPPER_FIELD_BOTTOM, c), TetrominoType.T);
        }
        for (int c = 7; c <= 9; c++) {
            board.place(new Position(Constants.LOWER_FIELD_TOP, c), TetrominoType.Z);
        }
        // 中央境界線を埋める
        fillRow(board, Constants.CENTER_ROW, TetrominoType.I);

        LineClearService.processClears(board);

        // 列が重ならないので row 10 に T と Z の両方が残り、追加連鎖は起きない
        for (int c = 0; c <= 2; c++) {
            assertThat(board.cellAt(Constants.CENTER_ROW, c)).isEqualTo(Cell.T);
        }
        for (int c = 7; c <= 9; c++) {
            assertThat(board.cellAt(Constants.CENTER_ROW, c)).isEqualTo(Cell.Z);
        }
        for (int c = 3; c <= 6; c++) {
            assertThat(board.cellAt(Constants.CENTER_ROW, c)).isEqualTo(Cell.EMPTY);
        }
    }

    private static void fillRow(Board board, int row, TetrominoType type) {
        for (int c = 0; c < Constants.BOARD_WIDTH; c++) {
            board.place(new Position(row, c), type);
        }
    }
}
