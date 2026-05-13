package com.example.tetris.game;

import com.example.tetris.domain.TetrominoType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HoldSlotTest {

    @Test
    void 新規スロットは空でholdできる() {
        HoldSlot slot = new HoldSlot();

        assertThat(slot.isEmpty()).isTrue();
        assertThat(slot.canHold()).isTrue();
    }

    @Test
    void 最初のswapは前のホールドがnullを返す() {
        HoldSlot slot = new HoldSlot();

        TetrominoType previous = slot.swap(TetrominoType.T);

        assertThat(previous).isNull();
        assertThat(slot.isEmpty()).isFalse();
        assertThat(slot.type()).isEqualTo(TetrominoType.T);
    }

    @Test
    void swap後はロックされて再holdできない() {
        HoldSlot slot = new HoldSlot();

        slot.swap(TetrominoType.T);

        assertThat(slot.canHold()).isFalse();
        assertThatThrownBy(() -> slot.swap(TetrominoType.I))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unlock後は再びswapできる_2回目は前の型を返す() {
        HoldSlot slot = new HoldSlot();
        slot.swap(TetrominoType.T);

        slot.unlock();
        TetrominoType previous = slot.swap(TetrominoType.I);

        assertThat(previous).isEqualTo(TetrominoType.T);
        assertThat(slot.type()).isEqualTo(TetrominoType.I);
    }

    @Test
    void resetでスロットを初期化する() {
        HoldSlot slot = new HoldSlot();
        slot.swap(TetrominoType.T);

        slot.reset();

        assertThat(slot.isEmpty()).isTrue();
        assertThat(slot.canHold()).isTrue();
    }
}
