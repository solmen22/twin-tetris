package com.example.tetris.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RotationTest {

    @Test
    void 時計回りは順にSPAWN_RIGHT_HALF_LEFTを巡回する() {
        assertThat(Rotation.SPAWN.rotateCw()).isEqualTo(Rotation.RIGHT);
        assertThat(Rotation.RIGHT.rotateCw()).isEqualTo(Rotation.HALF);
        assertThat(Rotation.HALF.rotateCw()).isEqualTo(Rotation.LEFT);
        assertThat(Rotation.LEFT.rotateCw()).isEqualTo(Rotation.SPAWN);
    }

    @Test
    void 反時計回りは逆順に巡回する() {
        assertThat(Rotation.SPAWN.rotateCcw()).isEqualTo(Rotation.LEFT);
        assertThat(Rotation.LEFT.rotateCcw()).isEqualTo(Rotation.HALF);
        assertThat(Rotation.HALF.rotateCcw()).isEqualTo(Rotation.RIGHT);
        assertThat(Rotation.RIGHT.rotateCcw()).isEqualTo(Rotation.SPAWN);
    }

    @Test
    void 時計回り4回で元に戻る() {
        Rotation r = Rotation.SPAWN;
        for (int i = 0; i < 4; i++) {
            r = r.rotateCw();
        }
        assertThat(r).isEqualTo(Rotation.SPAWN);
    }
}
