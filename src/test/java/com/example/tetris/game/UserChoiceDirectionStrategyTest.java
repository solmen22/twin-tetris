package com.example.tetris.game;

import com.example.tetris.domain.Direction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserChoiceDirectionStrategyTest {

    @Test
    void デフォルトの初期方向はDOWN() {
        UserChoiceDirectionStrategy strategy = new UserChoiceDirectionStrategy();

        assertThat(strategy.pending()).isEqualTo(Direction.DOWN);
        assertThat(strategy.next()).isEqualTo(Direction.DOWN);
    }

    @Test
    void selectUpで保留方向がUPに切り替わる() {
        UserChoiceDirectionStrategy strategy = new UserChoiceDirectionStrategy();

        strategy.selectUp();

        assertThat(strategy.pending()).isEqualTo(Direction.UP);
        assertThat(strategy.next()).isEqualTo(Direction.UP);
    }

    @Test
    void nextは方向を消費しない_繰り返し同じ値を返す() {
        UserChoiceDirectionStrategy strategy = new UserChoiceDirectionStrategy(Direction.UP);

        assertThat(strategy.next()).isEqualTo(Direction.UP);
        assertThat(strategy.next()).isEqualTo(Direction.UP);
        assertThat(strategy.pending()).isEqualTo(Direction.UP);
    }

    @Test
    void selectDownでDOWNに戻せる() {
        UserChoiceDirectionStrategy strategy = new UserChoiceDirectionStrategy(Direction.UP);

        strategy.selectDown();

        assertThat(strategy.pending()).isEqualTo(Direction.DOWN);
    }
}
