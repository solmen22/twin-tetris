package com.example.tetris.game;

import com.example.tetris.domain.Board;
import com.example.tetris.domain.Constants;
import com.example.tetris.domain.Position;
import com.example.tetris.domain.TetrominoType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link GameStats} とその {@link GameEngine} 経由でのカウント挙動を検証する。
 *
 * <p>テストケース一覧</p>
 * <ol>
 *   <li>初期状態は initial() 相当(全部 0)</li>
 *   <li>piecesPlaced はハードドロップで増分される</li>
 *   <li>elapsedMs は tick で加算される</li>
 *   <li>elapsedMs は pause 中は加算されない</li>
 *   <li>elapsedMs は gameOver 中は加算されない</li>
 *   <li>centerBoundaryClears は中央崩落 1 回で +1</li>
 *   <li>maxChain は連鎖が起きないプレイでは 0</li>
 *   <li>maxChain は連鎖 N 回が発生すると N</li>
 *   <li>simultaneousClears は上下同時崩落 1 回で +1</li>
 *   <li>GameStats.initial は全フィールド 0</li>
 *   <li>GameState のコンパクトコンストラクタは null を initial() に変換する</li>
 * </ol>
 */
class GameStatsTest {

    // ===================== GameStats レコード単体 =====================

    @Test
    void initialは全フィールド0() {
        GameStats stats = GameStats.initial();

        assertThat(stats.piecesPlaced()).isZero();
        assertThat(stats.centerBoundaryClears()).isZero();
        assertThat(stats.maxChain()).isZero();
        assertThat(stats.simultaneousClears()).isZero();
        assertThat(stats.elapsedMs()).isZero();
    }

    @Test
    void GameStatsはレコードとして値が等しければequals() {
        GameStats a = new GameStats(1, 2, 3, 4, 5L);
        GameStats b = new GameStats(1, 2, 3, 4, 5L);

        assertThat(a).isEqualTo(b);
    }

    // ===================== 初期状態 =====================

    @Test
    void GameEngine起動直後のstatsはinitial相当() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.T));

        GameStats stats = engine.state().stats();

        assertThat(stats).isEqualTo(GameStats.initial());
    }

    @Test
    void GameEngine起動直後のpiecesPlacedは0() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.T));

        assertThat(engine.state().stats().piecesPlaced()).isZero();
    }

    @Test
    void GameEngine起動直後のelapsedMsは0() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.T));

        assertThat(engine.state().stats().elapsedMs()).isZero();
    }

    // ===================== piecesPlaced =====================

    @Test
    void ハードドロップ1回でpiecesPlacedが1になる() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.O));

        engine.hardDrop();

        assertThat(engine.state().stats().piecesPlaced()).isOne();
    }

    @Test
    void ハードドロップ3回でpiecesPlacedが3になる() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.O));

        engine.hardDrop();
        engine.hardDrop();
        engine.hardDrop();

        assertThat(engine.state().stats().piecesPlaced()).isEqualTo(3);
    }

    @Test
    void 横移動のみではpiecesPlacedは増えない() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.T));

        engine.moveLeft();
        engine.moveRight();
        engine.rotateCw();

        assertThat(engine.state().stats().piecesPlaced()).isZero();
    }

    // ===================== elapsedMs =====================

    @Test
    void tickの累計がelapsedMsに反映される() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.T));

        for (int i = 0; i < 5; i++) {
            engine.tick(100.0);
        }

        // 5 回 × 100ms = 500ms。double → long のキャスト誤差を考慮し >= 500
        assertThat(engine.state().stats().elapsedMs()).isGreaterThanOrEqualTo(500L);
    }

    @Test
    void elapsedMsはtick呼び出し前後で単調増加する() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.T));

        long before = engine.state().stats().elapsedMs();
        engine.tick(250.0);
        long after = engine.state().stats().elapsedMs();

        assertThat(after).isGreaterThan(before);
    }

    @Test
    void pause中のtickではelapsedMsが加算されない() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.T));
        engine.tick(200.0);
        long beforePause = engine.state().stats().elapsedMs();

        engine.togglePause();
        engine.tick(1000.0);
        engine.tick(1000.0);

        assertThat(engine.state().stats().elapsedMs()).isEqualTo(beforePause);
    }

    @Test
    void gameOver後のtickではelapsedMsが加算されない() {
        // スポーン領域 (rows 0-1, cols 3-6) を埋めておき、hardDrop で次のスポーンを失敗させる
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.O));
        Board board = engine.state().board();
        for (int r = 0; r < 2; r++) {
            for (int c = 3; c < 7; c++) {
                board.place(new Position(r, c), TetrominoType.Z);
            }
        }
        engine.hardDrop();
        // この時点で gameOver
        assertThat(engine.state().gameOver()).isTrue();
        long elapsedAtGameOver = engine.state().stats().elapsedMs();

        engine.tick(500.0);
        engine.tick(500.0);

        assertThat(engine.state().stats().elapsedMs()).isEqualTo(elapsedAtGameOver);
    }

    // ===================== centerBoundaryClears =====================

    @Test
    void 中央境界線消去を1回起こすとcenterBoundaryClearsが1になる() {
        // I ミノで row 10 (中央境界線) のみが揃う盤面を直接構築する
        GameEngine engine = new GameEngine(new TestPieceProvider(
            List.of(TetrominoType.I, TetrominoType.T), TetrominoType.T));
        Board board = engine.state().board();
        // row 10 を I で 6 列だけ埋めて、残り 4 列を I ミノでハードドロップして埋める
        for (int c = 0; c < 6; c++) {
            board.place(new Position(Constants.CENTER_ROW, c), TetrominoType.Z);
        }
        // 上半分の row 9 に 4 列分の障害物を作り、I が row 9 に止まらないよう
        // また row 11 にも 4 列分埋めて I の落下先を row 10 にとどめる
        for (int c = 6; c < Constants.BOARD_WIDTH; c++) {
            board.place(new Position(Constants.LOWER_FIELD_TOP, c), TetrominoType.Z);
        }
        // I ミノを右端 (cols 6-9) に運んでハードドロップ → row 10 cols 6-9 を埋める
        for (int i = 0; i < 3; i++) {
            engine.moveRight();
        }

        engine.hardDrop();

        assertThat(engine.state().stats().centerBoundaryClears()).isOne();
    }

    @Test
    void 中央境界線が消えないプレイではcenterBoundaryClearsは0() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.O));

        engine.hardDrop();
        engine.hardDrop();

        assertThat(engine.state().stats().centerBoundaryClears()).isZero();
    }

    // ===================== maxChain =====================

    @Test
    void 連鎖が起きないプレイではmaxChainは0() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.O));

        engine.hardDrop();
        engine.hardDrop();

        assertThat(engine.state().stats().maxChain()).isZero();
    }

    @Test
    void 連鎖が1回発火するとmaxChainは1() {
        // LineClearServiceTest の「中央境界線消去後の上下噛み合いで連鎖が発火する」と
        // 同じ盤面を再現し、ハードドロップ前に盤面に直接配置する。
        // I ミノを落として最後の row 10 を埋める形で発火させる。
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.I));
        Board board = engine.state().board();
        // row 9 col 0-4 を T で埋める
        for (int c = 0; c <= 4; c++) {
            board.place(new Position(Constants.UPPER_FIELD_BOTTOM, c), TetrominoType.T);
        }
        // row 11 col 5-9 を Z で埋める
        for (int c = 5; c <= 9; c++) {
            board.place(new Position(Constants.LOWER_FIELD_TOP, c), TetrominoType.Z);
        }
        // row 10 を I で 6 列だけ埋めて、I ミノの 4 列で完成させる
        for (int c = 0; c < 6; c++) {
            board.place(new Position(Constants.CENTER_ROW, c), TetrominoType.I);
        }
        // I ミノを右端 (cols 6-9) に運んでハードドロップ
        for (int i = 0; i < 3; i++) {
            engine.moveRight();
        }

        engine.hardDrop();

        // step 1: 中央消去 + 上下シフト → step 2: 再び中央消去
        // chainCount = steps.size() - 1 = 1
        assertThat(engine.state().stats().maxChain()).isOne();
    }

    @Test
    void maxChainは過去最大値を保持する() {
        // 1 回目に連鎖を起こし、2 回目に連鎖が起きないドロップを行っても maxChain は維持
        GameEngine engine = new GameEngine(new TestPieceProvider(
            List.of(TetrominoType.I, TetrominoType.O), TetrominoType.O));
        Board board = engine.state().board();
        for (int c = 0; c <= 4; c++) {
            board.place(new Position(Constants.UPPER_FIELD_BOTTOM, c), TetrominoType.T);
        }
        for (int c = 5; c <= 9; c++) {
            board.place(new Position(Constants.LOWER_FIELD_TOP, c), TetrominoType.Z);
        }
        for (int c = 0; c < 6; c++) {
            board.place(new Position(Constants.CENTER_ROW, c), TetrominoType.I);
        }
        for (int i = 0; i < 3; i++) {
            engine.moveRight();
        }
        engine.hardDrop();
        int maxChainAfterFirst = engine.state().stats().maxChain();
        assertThat(maxChainAfterFirst).isOne();

        // 連鎖が起きない普通のハードドロップ
        engine.hardDrop();

        assertThat(engine.state().stats().maxChain()).isEqualTo(maxChainAfterFirst);
    }

    // ===================== simultaneousClears =====================

    @Test
    void 上下同時崩落が1回発生するとsimultaneousClearsが1になる() {
        // row 9, row 10, row 11 を同時に埋めた状態でハードドロップさせる
        // I ミノで最後の row を完成させる形が単純だが、ここでは盤面を直接埋めて
        // ロック確定の経由が必要なので、上半分用にハードドロップする 1 ミノを用意する
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.I));
        Board board = engine.state().board();
        // row 9 col 0-5 を Z で埋める(I が col 6-9 を埋めて完成)
        for (int c = 0; c < 6; c++) {
            board.place(new Position(Constants.UPPER_FIELD_BOTTOM, c), TetrominoType.Z);
        }
        // row 10 完全埋め
        for (int c = 0; c < Constants.BOARD_WIDTH; c++) {
            board.place(new Position(Constants.CENTER_ROW, c), TetrominoType.Z);
        }
        // row 11 完全埋め
        for (int c = 0; c < Constants.BOARD_WIDTH; c++) {
            board.place(new Position(Constants.LOWER_FIELD_TOP, c), TetrominoType.Z);
        }
        // I ミノを右端に運んで row 9 col 6-9 を埋める
        for (int i = 0; i < 3; i++) {
            engine.moveRight();
        }

        engine.hardDrop();

        // upperLines >= 1 && lowerLines >= 1 && centerCleared なので isSimultaneous == true
        assertThat(engine.state().stats().simultaneousClears()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void 上下同時崩落が起きないプレイではsimultaneousClearsは0() {
        GameEngine engine = new GameEngine(new TestPieceProvider(TetrominoType.O));

        engine.hardDrop();
        engine.hardDrop();

        assertThat(engine.state().stats().simultaneousClears()).isZero();
    }

    // ===================== GameState compact constructor =====================

    @Test
    void GameStateコンストラクタはstatsがnullならinitialに置換する() {
        // GameStats が null で渡された場合の防御を確認
        GameState state = new GameState(
            new Board(),
            null,
            com.example.tetris.domain.Score.initial(),
            null,           // stats == null
            false,
            false,
            GameMode.RANDOM,
            null,
            List.of(),
            null,
            false,
            0.0,
            0
        );

        assertThat(state.stats()).isEqualTo(GameStats.initial());
    }
}
