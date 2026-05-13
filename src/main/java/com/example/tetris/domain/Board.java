package com.example.tetris.domain;

import java.util.ArrayList;
import java.util.List;

public final class Board {

    private final int width;
    private final int height;
    private final Cell[][] cells;

    public Board() {
        this(Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT);
    }

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[height][width];
        clear();
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public void clear() {
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                cells[r][c] = Cell.EMPTY;
            }
        }
    }

    public Cell cellAt(int row, int col) {
        return cells[row][col];
    }

    public Cell cellAt(Position p) {
        return cells[p.row()][p.col()];
    }

    public boolean isInside(Position p) {
        return p.row() >= 0 && p.row() < height
            && p.col() >= 0 && p.col() < width;
    }

    public boolean isInside(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width;
    }

    public boolean isEmpty(Position p) {
        return isInside(p) && cells[p.row()][p.col()].isEmpty();
    }

    public boolean isEmpty(int row, int col) {
        return isInside(row, col) && cells[row][col].isEmpty();
    }

    public void place(Position p, TetrominoType type) {
        if (!isInside(p)) {
            throw new IllegalArgumentException("Position out of bounds: " + p);
        }
        cells[p.row()][p.col()] = Cell.of(type);
    }

    public void clearAt(Position p) {
        if (!isInside(p)) {
            return;
        }
        cells[p.row()][p.col()] = Cell.EMPTY;
    }

    public boolean isRowFull(int row) {
        if (row < 0 || row >= height) {
            return false;
        }
        for (int c = 0; c < width; c++) {
            if (cells[row][c].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public List<Cell> rowSnapshot(int row) {
        List<Cell> snapshot = new ArrayList<>(width);
        for (int c = 0; c < width; c++) {
            snapshot.add(cells[row][c]);
        }
        return snapshot;
    }

    public void setRow(int row, List<Cell> rowCells) {
        if (rowCells.size() != width) {
            throw new IllegalArgumentException("Row size mismatch: " + rowCells.size());
        }
        for (int c = 0; c < width; c++) {
            cells[row][c] = rowCells.get(c);
        }
    }

    public void clearRow(int row) {
        for (int c = 0; c < width; c++) {
            cells[row][c] = Cell.EMPTY;
        }
    }

    public Board copy() {
        Board b = new Board(width, height);
        for (int r = 0; r < height; r++) {
            System.arraycopy(this.cells[r], 0, b.cells[r], 0, width);
        }
        return b;
    }
}
