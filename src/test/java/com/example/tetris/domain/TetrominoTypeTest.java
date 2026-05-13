package com.example.tetris.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TetrominoTypeTest {

    @Test
    void すべてのミノは4セルを持つ() {
        for (TetrominoType type : TetrominoType.values()) {
            for (Rotation r : Rotation.values()) {
                assertThat(type.cellsAt(r))
                    .as("%s at %s", type, r)
                    .hasSize(4);
            }
        }
    }

    @Test
    void Oミノは全回転で同じセルパターン() {
        List<Position> spawn = TetrominoType.O.cellsAt(Rotation.SPAWN);
        for (Rotation r : Rotation.values()) {
            assertThat(TetrominoType.O.cellsAt(r)).containsExactlyElementsOf(spawn);
        }
    }

    @Test
    void Iミノは初期状態でcol0からcol3の水平4セル() {
        List<Position> cells = TetrominoType.I.cellsAt(Rotation.SPAWN);
        assertThat(cells).containsExactlyInAnyOrder(
            new Position(1, 0),
            new Position(1, 1),
            new Position(1, 2),
            new Position(1, 3)
        );
    }

    @Test
    void Tミノの初期状態は上に1点と下3点() {
        assertThat(TetrominoType.T.cellsAt(Rotation.SPAWN)).containsExactlyInAnyOrder(
            new Position(0, 1),
            new Position(1, 0),
            new Position(1, 1),
            new Position(1, 2)
        );
    }

    @Test
    void Sミノの初期状態() {
        assertThat(TetrominoType.S.cellsAt(Rotation.SPAWN)).containsExactlyInAnyOrder(
            new Position(0, 1),
            new Position(0, 2),
            new Position(1, 0),
            new Position(1, 1)
        );
    }

    @Test
    void Zミノの初期状態() {
        assertThat(TetrominoType.Z.cellsAt(Rotation.SPAWN)).containsExactlyInAnyOrder(
            new Position(0, 0),
            new Position(0, 1),
            new Position(1, 1),
            new Position(1, 2)
        );
    }

    @Test
    void Jミノの初期状態() {
        assertThat(TetrominoType.J.cellsAt(Rotation.SPAWN)).containsExactlyInAnyOrder(
            new Position(0, 0),
            new Position(1, 0),
            new Position(1, 1),
            new Position(1, 2)
        );
    }

    @Test
    void Lミノの初期状態() {
        assertThat(TetrominoType.L.cellsAt(Rotation.SPAWN)).containsExactlyInAnyOrder(
            new Position(0, 2),
            new Position(1, 0),
            new Position(1, 1),
            new Position(1, 2)
        );
    }

    @Test
    void Iミノの境界ボックスは4でその他は3() {
        assertThat(TetrominoType.I.boundingBoxSize()).isEqualTo(4);
        for (TetrominoType t : TetrominoType.values()) {
            if (t != TetrominoType.I) {
                assertThat(t.boundingBoxSize()).as("%s", t).isEqualTo(3);
            }
        }
    }

    @Test
    void 各ミノの色RGBが0以上255以下() {
        for (TetrominoType t : TetrominoType.values()) {
            assertThat(t.red()).isBetween(0, 255);
            assertThat(t.green()).isBetween(0, 255);
            assertThat(t.blue()).isBetween(0, 255);
        }
    }
}
