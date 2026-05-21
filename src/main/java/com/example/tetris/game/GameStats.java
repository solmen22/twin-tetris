package com.example.tetris.game;

/**
 * ゲーム実行中に蓄積される統計情報のスナップショット。
 *
 * <p>すべて累計値であり、{@link GameEngine} がロック確定や経過時間更新の
 * タイミングで増分する。値オブジェクト(record)なので不変。</p>
 *
 * @param piecesPlaced         ロック確定したミノの累計数
 * @param centerBoundaryClears 中央境界線が消去された累計回数
 *                             ({@code CascadeStep.centerCleared == true} の回数)
 * @param maxChain             1 回のロックで発生した連鎖の最大数
 *                             ({@code LineClearResult.chainCount()} の最大値)
 * @param simultaneousClears   上下同時崩落
 *                             ({@code CascadeStep.isSimultaneous() == true}) の発生数
 * @param elapsedMs            ゲーム開始からの経過時間(ms)。
 *                             pause / gameOver 中は加算しない
 */
public record GameStats(
    int piecesPlaced,
    int centerBoundaryClears,
    int maxChain,
    int simultaneousClears,
    long elapsedMs
) {

    public static GameStats initial() {
        return new GameStats(0, 0, 0, 0, 0L);
    }
}
