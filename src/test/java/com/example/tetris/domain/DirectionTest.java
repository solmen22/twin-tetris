package com.example.tetris.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DirectionTest {

    @Test
    void DOWNはrowStepが正で中央方向に進む() {
        assertThat(Direction.DOWN.rowStep()).isEqualTo(1);
    }

    @Test
    void UPはrowStepが負で中央方向に進む() {
        assertThat(Direction.UP.rowStep()).isEqualTo(-1);
    }

    @Test
    void oppositeは反対方向を返す() {
        assertThat(Direction.DOWN.opposite()).isEqualTo(Direction.UP);
        assertThat(Direction.UP.opposite()).isEqualTo(Direction.DOWN);
    }
}
