package com.example.tetris.domain;

public record Position(int row, int col) {

    public Position translate(int drow, int dcol) {
        return new Position(row + drow, col + dcol);
    }

    public Position withRow(int newRow) {
        return new Position(newRow, col);
    }

    public Position withCol(int newCol) {
        return new Position(row, newCol);
    }
}
