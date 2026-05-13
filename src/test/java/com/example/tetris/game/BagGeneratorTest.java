package com.example.tetris.game;

import com.example.tetris.domain.TetrominoType;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BagGeneratorTest {

    @Test
    void 一バッグで7種類すべてが必ず1回ずつ出る() {
        BagGenerator bag = new BagGenerator(new Random(42));

        Set<TetrominoType> seen = EnumSet.noneOf(TetrominoType.class);
        for (int i = 0; i < 7; i++) {
            seen.add(bag.next());
        }

        assertThat(seen).hasSize(7);
    }

    @Test
    void 七回引いた後はバッグが空になり次の7回で新しいバッグが開始() {
        BagGenerator bag = new BagGenerator(new Random(0));

        for (int i = 0; i < 7; i++) {
            bag.next();
        }
        assertThat(bag.remainingInCurrentBag()).isZero();

        TetrominoType eighth = bag.next();
        assertThat(eighth).isNotNull();
        assertThat(bag.remainingInCurrentBag()).isEqualTo(6);
    }

    @Test
    void 長期試行で各ミノ種類の出現回数が偏らない() {
        BagGenerator bag = new BagGenerator(new Random(123));
        Map<TetrominoType, Integer> counts = new EnumMap<>(TetrominoType.class);

        int iterations = 7 * 1000;
        for (int i = 0; i < iterations; i++) {
            counts.merge(bag.next(), 1, Integer::sum);
        }

        for (TetrominoType t : TetrominoType.values()) {
            assertThat(counts.get(t)).as("%s", t).isEqualTo(1000);
        }
    }
}
