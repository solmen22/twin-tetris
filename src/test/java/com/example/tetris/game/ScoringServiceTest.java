package com.example.tetris.game;

import org.junit.jupiter.api.Test;

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
    void 五ライン以上は0点で扱う() {
        assertThat(ScoringService.lineClearPoints(5, 1)).isZero();
    }

    @Test
    void ハードドロップは1セル2点() {
        assertThat(ScoringService.hardDropPoints(10)).isEqualTo(20);
    }

    @Test
    void ソフトドロップは1セル1点() {
        assertThat(ScoringService.softDropPoints(5)).isEqualTo(5);
    }
}
