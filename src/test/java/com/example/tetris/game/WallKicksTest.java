package com.example.tetris.game;

import com.example.tetris.domain.Rotation;
import com.example.tetris.domain.TetrominoType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WallKicksTest {

    @Test
    void OミノはオフセットなしのNO_KICKを返す() {
        int[][] kicks = WallKicks.offsetsForDown(TetrominoType.O, Rotation.SPAWN, Rotation.RIGHT);

        assertThat(kicks).hasDimensions(1, 2);
        assertThat(kicks[0]).containsExactly(0, 0);
    }

    @Test
    void JLSTZミノは各遷移で5回のキックテストを持つ() {
        for (TetrominoType type : new TetrominoType[] {
            TetrominoType.J, TetrominoType.L, TetrominoType.S, TetrominoType.T, TetrominoType.Z
        }) {
            int[][] kicks = WallKicks.offsetsForDown(type, Rotation.SPAWN, Rotation.RIGHT);
            assertThat(kicks).as("%s SPAWN→RIGHT", type).hasNumberOfRows(5);
        }
    }

    @Test
    void Iミノも各遷移で5回のキックテストを持つ() {
        int[][] kicks = WallKicks.offsetsForDown(TetrominoType.I, Rotation.SPAWN, Rotation.RIGHT);

        assertThat(kicks).hasNumberOfRows(5);
    }

    @Test
    void Iミノとその他のミノでキックテーブルが異なる() {
        int[][] iKicks = WallKicks.offsetsForDown(TetrominoType.I, Rotation.SPAWN, Rotation.RIGHT);
        int[][] tKicks = WallKicks.offsetsForDown(TetrominoType.T, Rotation.SPAWN, Rotation.RIGHT);

        assertThat(iKicks[1]).isNotEqualTo(tKicks[1]);
    }

    @Test
    void すべてのキックテストの先頭は0_0_即試行() {
        for (TetrominoType type : TetrominoType.values()) {
            for (Rotation from : Rotation.values()) {
                Rotation to = from.rotateCw();
                int[][] kicks = WallKicks.offsetsForDown(type, from, to);
                assertThat(kicks[0]).as("%s %s→%s", type, from, to).containsExactly(0, 0);
            }
        }
    }
}
