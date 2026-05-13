package com.example.tetris.game;

import com.example.tetris.domain.Constants;
import com.example.tetris.domain.Position;
import com.example.tetris.domain.TetrominoType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GameEngineTest {

    @Test
    void 起動直後は最初のミノが盤面上にスポーンされている() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.T));

        GameState state = engine.state();

        assertThat(state.currentPiece()).isNotNull();
        assertThat(state.currentPiece().type()).isEqualTo(TetrominoType.T);
        assertThat(state.gameOver()).isFalse();
    }

    @Test
    void moveLeftで現在ミノが1列左に動く() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.T));
        Position before = engine.state().currentPiece().origin();

        engine.moveLeft();

        assertThat(engine.state().currentPiece().origin().col()).isEqualTo(before.col() - 1);
    }

    @Test
    void moveRightで現在ミノが1列右に動く() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.T));
        Position before = engine.state().currentPiece().origin();

        engine.moveRight();

        assertThat(engine.state().currentPiece().origin().col()).isEqualTo(before.col() + 1);
    }

    @Test
    void hardDropで現在ミノは中央境界線まで一気に落ちロックされる() {
        GameEngine engine = new GameEngine(new TestPieceProvider(
            List.of(TetrominoType.O, TetrominoType.T), TetrominoType.T));

        engine.hardDrop();

        // Oミノが固定 → 次のミノ (T) がスポーンしている
        assertThat(engine.state().currentPiece().type()).isEqualTo(TetrominoType.T);
        // 元の O が中央境界線付近に固定されている (origin row 9 → cells at rows 9,10)
        assertThat(engine.state().board().cellAt(Constants.CENTER_ROW, 4).isFilled()).isTrue();
    }

    @Test
    void hardDropはセルあたり2点を加算する() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.O));
        long before = engine.state().score().points();

        engine.hardDrop();

        // Oミノは row 0 から row 9 まで 9 セル落下、× 2 点 = 18 点
        assertThat(engine.state().score().points()).isEqualTo(before + 18);
    }

    @Test
    void 連続ハードドロップでブロックが下から積み上がる() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.O));

        engine.hardDrop();
        engine.hardDrop();

        // O ミノ 2 個が積み重なって rows 7-10 が部分的に埋まっているはず
        assertThat(engine.state().board().cellAt(Constants.CENTER_ROW, 4).isFilled()).isTrue();
        assertThat(engine.state().board().cellAt(Constants.CENTER_ROW - 2, 4).isFilled()).isTrue();
    }

    @Test
    void rotateCwで現在ミノの回転状態がCWに変わる() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.T));
        var before = engine.state().currentPiece().rotation();

        engine.rotateCw();

        assertThat(engine.state().currentPiece().rotation()).isEqualTo(before.rotateCw());
    }

    @Test
    void tickで時間経過に応じて自動落下する() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.T));
        int beforeRow = engine.state().currentPiece().origin().row();

        // レベル1は約1秒/セル、十分大きい dt を与えて確実に1段以上落とす
        engine.tick(2000);

        assertThat(engine.state().currentPiece().origin().row()).isGreaterThan(beforeRow);
    }

    @Test
    void pause中はtickでミノが動かない() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.T));
        engine.togglePause();
        int beforeRow = engine.state().currentPiece().origin().row();

        engine.tick(5000);

        assertThat(engine.state().currentPiece().origin().row()).isEqualTo(beforeRow);
    }

    @Test
    void スポーン位置が埋まっているとゲームオーバー() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.O));
        var board = engine.state().board();
        // スポーン領域 (rows 0-1, cols 3-6) を埋めて、次のスポーンを失敗させる
        for (int r = 0; r < 2; r++) {
            for (int c = 3; c < 7; c++) {
                board.place(new Position(r, c), TetrominoType.Z);
            }
        }

        engine.hardDrop();

        assertThat(engine.state().gameOver()).isTrue();
    }

    @Test
    void ラインが揃えばスコアとライン数が加算される() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.I));
        var board = engine.state().board();
        // 上半分の最下行 row 9 の cols 0-5 を Z で埋める
        for (int c = 0; c < 6; c++) {
            board.place(new Position(Constants.UPPER_FIELD_BOTTOM, c), TetrominoType.Z);
        }
        // 中央境界線 row 10 の cols 6-9 を埋めて、I ミノが row 10 に到達するのを防ぐ
        for (int c = 6; c < Constants.BOARD_WIDTH; c++) {
            board.place(new Position(Constants.CENTER_ROW, c), TetrominoType.Z);
        }
        // 現在の I ミノを右端 (cols 6-9) に運んでハードドロップ
        for (int i = 0; i < 3; i++) {
            engine.moveRight();
        }

        engine.hardDrop();

        assertThat(engine.state().score().lines()).isEqualTo(1);
        assertThat(engine.state().score().points()).isGreaterThan(0);
    }
}
