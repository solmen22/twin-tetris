package com.example.tetris.domain;

import java.util.ArrayList;
import java.util.List;

public record Tetromino(TetrominoType type, Position origin, Rotation rotation, Direction direction) {

    public static Tetromino spawnDown(TetrominoType type) {
        return new Tetromino(
            type,
            new Position(Constants.SPAWN_DOWN_ROW, Constants.SPAWN_COL),
            Rotation.SPAWN,
            Direction.DOWN
        );
    }

    public static Tetromino spawnUp(TetrominoType type) {
        int maxLocalRow = type.maxLocalRow(Rotation.HALF);
        int originRow = Constants.BOARD_HEIGHT - 1 - maxLocalRow;
        return new Tetromino(
            type,
            new Position(originRow, Constants.SPAWN_COL),
            Rotation.HALF,
            Direction.UP
        );
    }

    public List<Position> cells() {
        List<Position> local = type.cellsAt(rotation);
        List<Position> result = new ArrayList<>(local.size());
        for (Position p : local) {
            result.add(new Position(origin.row() + p.row(), origin.col() + p.col()));
        }
        return result;
    }

    public Tetromino translated(int drow, int dcol) {
        return new Tetromino(type, origin.translate(drow, dcol), rotation, direction);
    }

    public Tetromino withRotation(Rotation newRotation) {
        return new Tetromino(type, origin, newRotation, direction);
    }

    public Tetromino withOrigin(Position newOrigin) {
        return new Tetromino(type, newOrigin, rotation, direction);
    }
}
