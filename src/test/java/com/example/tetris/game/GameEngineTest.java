package com.example.tetris.game;

import com.example.tetris.domain.Constants;
import com.example.tetris.domain.Direction;
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
    void Alternating戦略では最初DOWN次UPがスポーンされる() {
        GameEngine engine = new GameEngine(
            new TestPieceProvider(List.of(TetrominoType.T, TetrominoType.T), TetrominoType.T),
            DirectionStrategy.alternating()
        );

        assertThat(engine.state().currentPiece().direction()).isEqualTo(Direction.DOWN);
        engine.hardDrop();
        assertThat(engine.state().currentPiece().direction()).isEqualTo(Direction.UP);
    }

    @Test
    void UPミノはtickで上方向に上昇する() {
        GameEngine engine = new GameEngine(
            new TestPieceProvider(List.of(TetrominoType.T), TetrominoType.T),
            new AlternatingDirectionStrategy(Direction.UP)
        );
        int beforeRow = engine.state().currentPiece().origin().row();

        engine.tick(2000);

        assertThat(engine.state().currentPiece().origin().row()).isLessThan(beforeRow);
    }

    @Test
    void UPミノのハードドロップは中央境界線に到達して固定される() {
        GameEngine engine = new GameEngine(
            new TestPieceProvider(List.of(TetrominoType.O, TetrominoType.O), TetrominoType.O),
            new AlternatingDirectionStrategy(Direction.UP)
        );

        engine.hardDrop();

        // O ミノは UP のとき row 19-20 で発生し、上昇して中央境界線で止まる
        // 1 個目の O が row 10-11 のあたりに固定される
        assertThat(engine.state().board().cellAt(Constants.CENTER_ROW, 4).isFilled()).isTrue();
    }

    @Test
    void UPミノのhardDropもセルあたり2点() {
        GameEngine engine = new GameEngine(
            new TestPieceProvider(List.of(TetrominoType.O, TetrominoType.O), TetrominoType.O),
            new AlternatingDirectionStrategy(Direction.UP)
        );
        long before = engine.state().score().points();

        engine.hardDrop();

        // O ミノは spawnUp で row 19-20 (origin row 19) → row 10-11 (origin row 10) まで 9 セル上昇
        assertThat(engine.state().score().points()).isEqualTo(before + 18);
    }

    @Test
    void GameStateはNEXTキューの先頭3個を露出する() {
        TestPieceProvider provider = new TestPieceProvider(
            List.of(TetrominoType.I, TetrominoType.O, TetrominoType.T, TetrominoType.S, TetrominoType.Z),
            TetrominoType.J
        );

        GameEngine engine = new GameEngine(provider, DirectionStrategy.alwaysDown(), GameMode.RANDOM);

        // 最初のミノは I が現在ミノに。next 3 は O,T,S
        assertThat(engine.state().currentPiece().type()).isEqualTo(TetrominoType.I);
        assertThat(engine.state().nextQueue()).containsExactly(TetrominoType.O, TetrominoType.T, TetrominoType.S);
    }

    @Test
    void hold_スロットが空ならNEXTから次が出る() {
        TestPieceProvider provider = new TestPieceProvider(
            List.of(TetrominoType.I, TetrominoType.O, TetrominoType.T, TetrominoType.S),
            TetrominoType.Z
        );
        GameEngine engine = new GameEngine(provider, DirectionStrategy.alwaysDown(), GameMode.RANDOM);
        // I が現在
        assertThat(engine.state().currentPiece().type()).isEqualTo(TetrominoType.I);

        engine.hold();

        // I が hold、O が現在ミノ
        assertThat(engine.state().heldType()).isEqualTo(TetrominoType.I);
        assertThat(engine.state().currentPiece().type()).isEqualTo(TetrominoType.O);
    }

    @Test
    void hold_スロットに既にあれば現在ミノと交換() {
        TestPieceProvider provider = new TestPieceProvider(
            List.of(TetrominoType.I, TetrominoType.O, TetrominoType.T),
            TetrominoType.S
        );
        GameEngine engine = new GameEngine(provider, DirectionStrategy.alwaysDown(), GameMode.RANDOM);
        engine.hold();
        // I が hold、O が現在
        engine.hardDrop();  // O ロック → T 出現、hold ロック解除
        // T が現在、I が hold

        engine.hold();

        // T が hold、I が現在ミノとして復活
        assertThat(engine.state().heldType()).isEqualTo(TetrominoType.T);
        assertThat(engine.state().currentPiece().type()).isEqualTo(TetrominoType.I);
    }

    @Test
    void hold_連続実行は禁止される() {
        TestPieceProvider provider = new TestPieceProvider(
            List.of(TetrominoType.I, TetrominoType.O, TetrominoType.T),
            TetrominoType.S
        );
        GameEngine engine = new GameEngine(provider, DirectionStrategy.alwaysDown(), GameMode.RANDOM);

        engine.hold();  // I → hold、O が現在
        TetrominoType heldBefore = engine.state().heldType();
        TetrominoType currentBefore = engine.state().currentPiece().type();

        engine.hold();  // ロックされているので無視

        assertThat(engine.state().heldType()).isEqualTo(heldBefore);
        assertThat(engine.state().currentPiece().type()).isEqualTo(currentBefore);
    }

    @Test
    void hold_ロックは次のスポーン後に解除される() {
        TestPieceProvider provider = new TestPieceProvider(
            List.of(TetrominoType.I, TetrominoType.O, TetrominoType.T, TetrominoType.S),
            TetrominoType.Z
        );
        GameEngine engine = new GameEngine(provider, DirectionStrategy.alwaysDown(), GameMode.RANDOM);

        engine.hold();      // I → hold、O が現在 (ロック中)
        engine.hardDrop();  // O ロック → T 出現、hold 解除
        engine.hold();      // T → hold、I が復活

        assertThat(engine.state().heldType()).isEqualTo(TetrominoType.T);
        assertThat(engine.state().currentPiece().type()).isEqualTo(TetrominoType.I);
    }

    @Test
    void USER_CHOICE_モードのstateはpendingDirectionを露出する() {
        GameEngine engine = new GameEngine(
            new TestPieceProvider(TetrominoType.T),
            DirectionStrategy.userChoice(),
            GameMode.USER_CHOICE
        );

        assertThat(engine.state().pendingDirection()).isEqualTo(Direction.DOWN);
        assertThat(engine.state().mode()).isEqualTo(GameMode.USER_CHOICE);
    }

    @Test
    void USER_CHOICE_モードのselectDirectionUpは保留方向を切り替える() {
        UserChoiceDirectionStrategy strategy = DirectionStrategy.userChoice();
        GameEngine engine = new GameEngine(
            new TestPieceProvider(TetrominoType.T),
            strategy,
            GameMode.USER_CHOICE
        );

        engine.selectDirectionUp();

        assertThat(strategy.pending()).isEqualTo(Direction.UP);
        assertThat(engine.state().pendingDirection()).isEqualTo(Direction.UP);
    }

    @Test
    void USER_CHOICE_モードのspawnGrace中はselectDirectionで現在ミノが再生成される() {
        GameEngine engine = new GameEngine(
            new TestPieceProvider(TetrominoType.T),
            DirectionStrategy.userChoice(),
            GameMode.USER_CHOICE
        );

        // 起動直後は grace 中 (initial spawn)
        assertThat(engine.state().inSpawnGrace()).isTrue();
        assertThat(engine.state().currentPiece().direction()).isEqualTo(Direction.DOWN);

        engine.selectDirectionUp();

        // grace 中なので現在ミノが UP で再スポーン
        assertThat(engine.state().currentPiece().direction()).isEqualTo(Direction.UP);
    }

    @Test
    void USER_CHOICE_猶予が切れても方向切替で現在ミノが即座に切り替わる() {
        GameEngine engine = new GameEngine(
            new TestPieceProvider(TetrominoType.T),
            DirectionStrategy.userChoice(),
            GameMode.USER_CHOICE
        );
        // 猶予時間(500ms)を終わらせる
        engine.tick(600);
        assertThat(engine.state().inSpawnGrace()).isFalse();
        assertThat(engine.state().currentPiece().direction()).isEqualTo(Direction.DOWN);

        engine.selectDirectionUp();

        // 猶予外でも、今落下中のミノが即座に UP へ切り替わる
        assertThat(engine.state().currentPiece().direction()).isEqualTo(Direction.UP);

        engine.selectDirectionDown();

        // もう一度押せば DOWN に戻せる
        assertThat(engine.state().currentPiece().direction()).isEqualTo(Direction.DOWN);
    }

    @Test
    void USER_CHOICE_grace中の方向反転が衝突する場合はゲームオーバーにならず現在ミノを維持() {
        GameEngine engine = new GameEngine(
            new TestPieceProvider(TetrominoType.T),
            DirectionStrategy.userChoice(),
            GameMode.USER_CHOICE
        );
        var board = engine.state().board();
        // UP ミノのスポーン領域 (rows 19-20, cols 3-6) を埋めておく
        for (int r = 19; r <= 20; r++) {
            for (int c = 3; c < 7; c++) {
                board.place(new Position(r, c), TetrominoType.Z);
            }
        }
        assertThat(engine.state().inSpawnGrace()).isTrue();
        assertThat(engine.state().currentPiece().direction()).isEqualTo(Direction.DOWN);

        engine.selectDirectionUp();

        // 反転すると衝突するため、ゲームオーバーにならず DOWN ミノが維持される
        assertThat(engine.state().gameOver()).isFalse();
        assertThat(engine.state().currentPiece().direction()).isEqualTo(Direction.DOWN);
    }

    @Test
    void USER_CHOICE_モードのspawnGraceは500ms経過で終了() {
        GameEngine engine = new GameEngine(
            new TestPieceProvider(TetrominoType.T),
            DirectionStrategy.userChoice(),
            GameMode.USER_CHOICE
        );

        assertThat(engine.state().inSpawnGrace()).isTrue();

        engine.tick(501);

        assertThat(engine.state().inSpawnGrace()).isFalse();
    }

    @Test
    void RANDOM_モードはspawnGraceに入らない() {
        GameEngine engine = new GameEngine(
            new TestPieceProvider(TetrominoType.T),
            DirectionStrategy.alwaysDown(),
            GameMode.RANDOM
        );

        assertThat(engine.state().inSpawnGrace()).isFalse();
    }

    @Test
    void 接地してもロックディレイ満了までは固定されない() {
        GameEngine engine = new GameEngine(new TestPieceProvider(
            List.of(TetrominoType.O, TetrominoType.T), TetrominoType.T));
        // ソフトドロップで接地まで落とす(余分な呼び出しは衝突で無視される)
        for (int i = 0; i < 20; i++) {
            engine.softDrop();
        }
        // まだ O のまま(接地直後はロックされていない)
        assertThat(engine.state().currentPiece().type()).isEqualTo(TetrominoType.O);

        // ロックタイマー開始 + 短い経過ではまだ固定されない
        engine.tick(10);
        engine.tick(300);
        assertThat(engine.state().currentPiece().type()).isEqualTo(TetrominoType.O);

        // ロックディレイ(500ms)を超えると固定され次のミノ T が出現
        engine.tick(300);
        assertThat(engine.state().currentPiece().type()).isEqualTo(TetrominoType.T);
    }

    @Test
    void 接地中の移動でロックディレイがリセットされる() {
        GameEngine engine = new GameEngine(new TestPieceProvider(
            List.of(TetrominoType.O, TetrominoType.T), TetrominoType.T));
        for (int i = 0; i < 20; i++) {
            engine.softDrop();
        }

        engine.tick(10);    // ロック開始、タイマー 500
        engine.tick(400);   // タイマー 100
        engine.moveLeft();  // 接地中の移動 → タイマー 500 に再チャージ
        engine.tick(400);   // タイマー 100(リセットされたのでまだ固定されない)
        assertThat(engine.state().currentPiece().type()).isEqualTo(TetrominoType.O);

        engine.tick(200);   // 満了 → 固定 → T 出現
        assertThat(engine.state().currentPiece().type()).isEqualTo(TetrominoType.T);
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
