package com.example.tetris.game;

import com.example.tetris.game.LineClearService.CascadeStep;
import com.example.tetris.game.LineClearService.LineClearResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScoringServiceTest {

    @Test
    void レベル1のシングルは100点() {
        assertThat(ScoringService.lineClearPoints(1, 1)).isEqualTo(100);
    }

    @Test
    void レベル1のダブルは300点() {
        assertThat(ScoringService.lineClearPoints(2, 1)).isEqualTo(300);
    }

    @Test
    void レベル1のトリプルは500点() {
        assertThat(ScoringService.lineClearPoints(3, 1)).isEqualTo(500);
    }

    @Test
    void レベル1のテトリスは800点() {
        assertThat(ScoringService.lineClearPoints(4, 1)).isEqualTo(800);
    }

    @Test
    void レベル倍率が適用される() {
        assertThat(ScoringService.lineClearPoints(4, 3)).isEqualTo(2400);
    }

    @Test
    void ゼロラインは0点() {
        assertThat(ScoringService.lineClearPoints(0, 1)).isZero();
    }

    @Test
    void 五ライン以上は線形外挿_5ラインで1000点() {
        assertThat(ScoringService.lineClearPoints(5, 1)).isEqualTo(1000);
    }

    @Test
    void 九ラインで1800点_線形外挿() {
        assertThat(ScoringService.lineClearPoints(9, 1)).isEqualTo(1800);
    }

    @Test
    void ハードドロップは1セル2点() {
        assertThat(ScoringService.hardDropPoints(10)).isEqualTo(20);
    }

    @Test
    void ソフトドロップは1セル1点() {
        assertThat(ScoringService.softDropPoints(5)).isEqualTo(5);
    }

    @Test
    void 上半分のみ1ライン消去ステップは倍率1() {
        CascadeStep step = new CascadeStep(1, 0, false);

        assertThat(ScoringService.multiplierFor(step)).isOne();
        assertThat(ScoringService.stepPoints(step, 1)).isEqualTo(100);
    }

    @Test
    void 中央境界線消去のみは倍率2_基本100で200点() {
        CascadeStep step = new CascadeStep(0, 0, true);

        assertThat(ScoringService.multiplierFor(step)).isEqualTo(2);
        assertThat(ScoringService.stepPoints(step, 1)).isEqualTo(200);
    }

    @Test
    void 上半分_中央_の同時消去は中央扱いで倍率2() {
        CascadeStep step = new CascadeStep(2, 0, true);

        assertThat(ScoringService.multiplierFor(step)).isEqualTo(2);
        // 3 ライン基本500 × 2 = 1000
        assertThat(ScoringService.stepPoints(step, 1)).isEqualTo(1000);
    }

    @Test
    void 上下_中央_の同時消去は倍率5() {
        CascadeStep step = new CascadeStep(1, 1, true);

        assertThat(ScoringService.multiplierFor(step)).isEqualTo(5);
        // 3 ライン基本500 × 5 = 2500
        assertThat(ScoringService.stepPoints(step, 1)).isEqualTo(2500);
    }

    @Test
    void 連鎖0回はボーナス0() {
        assertThat(ScoringService.chainBonus(0)).isZero();
    }

    @Test
    void 連鎖1回は50点() {
        assertThat(ScoringService.chainBonus(1)).isEqualTo(50);
    }

    @Test
    void 連鎖5回は250点() {
        assertThat(ScoringService.chainBonus(5)).isEqualTo(250);
    }

    @Test
    void resultPointsは各ステップとREN加算を合算する() {
        // step1: 中央のみ消去 → 200点
        // step2: 上半分1ライン → 100点
        // 連鎖1回 → 50点
        LineClearResult result = new LineClearResult(List.of(
            new CascadeStep(0, 0, true),
            new CascadeStep(1, 0, false)
        ));

        assertThat(ScoringService.resultPoints(result, 1)).isEqualTo(200 + 100 + 50);
    }

    @Test
    void resultPointsはレベル倍率を全ステップに適用() {
        LineClearResult result = new LineClearResult(List.of(
            new CascadeStep(1, 0, false)  // 100点
        ));

        assertThat(ScoringService.resultPoints(result, 3)).isEqualTo(300);
    }
}
