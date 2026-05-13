package com.example.tetris.game;

import com.example.tetris.domain.Board;
import com.example.tetris.domain.Cell;
import com.example.tetris.domain.Constants;

import java.util.ArrayList;
import java.util.List;

public final class LineClearService {

    private LineClearService() {
    }

    public static int clearUpperHalf(Board board) {
        return clearRange(board, Constants.UPPER_FIELD_TOP, Constants.UPPER_FIELD_BOTTOM);
    }

    private static int clearRange(Board board, int topRow, int bottomRow) {
        List<List<Cell>> retainedRows = new ArrayList<>();
        int cleared = 0;

        for (int row = topRow; row <= bottomRow; row++) {
            if (board.isRowFull(row)) {
                cleared++;
            } else {
                retainedRows.add(board.rowSnapshot(row));
            }
        }
        if (cleared == 0) {
            return 0;
        }

        int writeRow = bottomRow;
        for (int i = retainedRows.size() - 1; i >= 0; i--) {
            board.setRow(writeRow, retainedRows.get(i));
            writeRow--;
        }
        while (writeRow >= topRow) {
            board.clearRow(writeRow);
            writeRow--;
        }
        return cleared;
    }
}
