package com.example.tetris.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreTest {

    @Test
    void 初期スコアは0点_0ライン_レベル1() {
        Score s = Score.initial();

        assertThat(s.points()).isZero();
        assertThat(s.lines()).isZero();
        assertThat(s.level()).isEqualTo(1);
    }

    @Test
    void addPointsは点数のみ加算する() {
        Score s = Score.initial().addPoints(800);

        assertThat(s.points()).isEqualTo(800);
        assertThat(s.lines()).isZero();
        assertThat(s.level()).isEqualTo(1);
    }

    @Test
    void addLinesは消去ライン数を加算する() {
        Score s = Score.initial().addLines(3);

        assertThat(s.lines()).isEqualTo(3);
    }

    @Test
    void 累計10ラインでレベル2にあがる() {
        Score s = Score.initial().addLines(10);

        assertThat(s.level()).isEqualTo(2);
    }

    @Test
    void 累計25ラインでレベル3() {
        Score s = Score.initial().addLines(25);

        assertThat(s.level()).isEqualTo(3);
    }
}
