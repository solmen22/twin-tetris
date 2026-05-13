package com.example.tetris.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TetrominoTest {

    @Test
    void spawnDownはDOWN方向で初期回転_スポーン行に配置される() {
        Tetromino piece = Tetromino.spawnDown(TetrominoType.T);

        assertThat(piece.type()).isEqualTo(TetrominoType.T);
        assertThat(piece.direction()).isEqualTo(Direction.DOWN);
        assertThat(piece.rotation()).isEqualTo(Rotation.SPAWN);
        assertThat(piece.origin()).isEqualTo(new Position(Constants.SPAWN_DOWN_ROW, Constants.SPAWN_DOWN_COL));
    }

    @Test
    void spawnUpはUP方向で半回転_下端付近に配置される() {
        Tetromino piece = Tetromino.spawnUp(TetrominoType.T);

        assertThat(piece.direction()).isEqualTo(Direction.UP);
        assertThat(piece.rotation()).isEqualTo(Rotation.HALF);
        assertThat(piece.origin()).isEqualTo(new Position(Constants.SPAWN_UP_ROW, Constants.SPAWN_UP_COL));
    }

    @Test
    void cellsはoriginを加算したワールド座標を返す() {
        Tetromino piece = new Tetromino(
            TetrominoType.T,
            new Position(5, 4),
            Rotation.SPAWN,
            Direction.DOWN
        );

        List<Position> cells = piece.cells();

        assertThat(cells).containsExactlyInAnyOrder(
            new Position(5, 5),
            new Position(6, 4),
            new Position(6, 5),
            new Position(6, 6)
        );
    }

    @Test
    void translatedは元の位置を変えず移動した新しいインスタンスを返す() {
        Tetromino piece = Tetromino.spawnDown(TetrominoType.I);

        Tetromino moved = piece.translated(3, -2);

        assertThat(moved.origin()).isEqualTo(piece.origin().translate(3, -2));
        assertThat(piece.origin()).isEqualTo(new Position(Constants.SPAWN_DOWN_ROW, Constants.SPAWN_DOWN_COL));
    }

    @Test
    void withRotationは回転状態のみ変更する() {
        Tetromino piece = Tetromino.spawnDown(TetrominoType.T);

        Tetromino rotated = piece.withRotation(Rotation.RIGHT);

        assertThat(rotated.rotation()).isEqualTo(Rotation.RIGHT);
        assertThat(rotated.origin()).isEqualTo(piece.origin());
        assertThat(rotated.type()).isEqualTo(piece.type());
    }
}
