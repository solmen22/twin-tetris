package com.example.tetris.game;

import com.example.tetris.domain.Direction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlternatingDirectionStrategyTest {

    @Test
    void デフォルトで初回はDOWN_その後DOWNとUPが交互() {
        AlternatingDirectionStrategy strategy = new AlternatingDirectionStrategy();

        assertThat(strategy.next()).isEqualTo(Direction.DOWN);
        assertThat(strategy.next()).isEqualTo(Direction.UP);
        assertThat(strategy.next()).isEqualTo(Direction.DOWN);
        assertThat(strategy.next()).isEqualTo(Direction.UP);
    }

    @Test
    void 初回をUPに設定できる() {
        AlternatingDirectionStrategy strategy = new AlternatingDirectionStrategy(Direction.UP);

        assertThat(strategy.next()).isEqualTo(Direction.UP);
        assertThat(strategy.next()).isEqualTo(Direction.DOWN);
    }

    @Test
    void DirectionStrategy_alternating_ファクトリも交互() {
        DirectionStrategy strategy = DirectionStrategy.alternating();

        Direction first = strategy.next();
        Direction second = strategy.next();

        assertThat(second).isEqualTo(first.opposite());
    }

    @Test
    void DirectionStrategy_alwaysDown_ファクトリは常にDOWN() {
        DirectionStrategy strategy = DirectionStrategy.alwaysDown();

        for (int i = 0; i < 5; i++) {
            assertThat(strategy.next()).isEqualTo(Direction.DOWN);
        }
    }
}
