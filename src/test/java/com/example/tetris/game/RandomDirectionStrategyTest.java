package com.example.tetris.game;

import com.example.tetris.domain.Direction;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class RandomDirectionStrategyTest {

    @Test
    void 戻り値は必ずDOWNまたはUP() {
        RandomDirectionStrategy strategy = new RandomDirectionStrategy(new Random(0));

        for (int i = 0; i < 100; i++) {
            Direction d = strategy.next();
            assertThat(d).isIn(Direction.DOWN, Direction.UP);
        }
    }

    @Test
    void 長期試行で両方向が概ね均等に出現() {
        RandomDirectionStrategy strategy = new RandomDirectionStrategy(new Random(7));
        Map<Direction, Integer> counts = new EnumMap<>(Direction.class);
        counts.put(Direction.DOWN, 0);
        counts.put(Direction.UP, 0);

        int trials = 10_000;
        for (int i = 0; i < trials; i++) {
            counts.merge(strategy.next(), 1, Integer::sum);
        }

        int down = counts.get(Direction.DOWN);
        int up = counts.get(Direction.UP);
        assertThat(down).isBetween(4500, 5500);
        assertThat(up).isBetween(4500, 5500);
    }
}
