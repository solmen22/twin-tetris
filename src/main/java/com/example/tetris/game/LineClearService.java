package com.example.tetris.game;

import com.example.tetris.domain.Board;
import com.example.tetris.domain.Cell;
import com.example.tetris.domain.Constants;

import java.util.ArrayList;
import java.util.List;

public final class LineClearService {

    public record CascadeStep(int upperLines, int lowerLines, boolean centerCleared) {

        public static final CascadeStep EMPTY = new CascadeStep(0, 0, false);

        public int totalLines() {
            return upperLines + lowerLines + (centerCleared ? 1 : 0);
        }

        public boolean isEmpty() {
            return totalLines() == 0;
        }

        public boolean isSimultaneous() {
            return centerCleared && upperLines > 0 && lowerLines > 0;
        }
    }

    public record LineClearResult(List<CascadeStep> steps) {

        public LineClearResult {
            steps = List.copyOf(steps);
        }

        public static LineClearResult empty() {
            return new LineClearResult(List.of());
        }

        public int totalLines() {
            return steps.stream().mapToInt(CascadeStep::totalLines).sum();
        }

        public int chainCount() {
            return Math.max(0, steps.size() - 1);
        }

        public boolean isEmpty() {
            return steps.isEmpty();
        }
    }

    private LineClearService() {
    }

    public static LineClearResult processClears(Board board) {
        List<CascadeStep> steps = new ArrayList<>();
        while (true) {
            CascadeStep step = performStep(board);
            if (step.isEmpty()) {
                break;
            }
            steps.add(step);
        }
        return new LineClearResult(steps);
    }

    private static CascadeStep performStep(Board board) {
        int upperLines = countFullRows(board, Constants.UPPER_FIELD_TOP, Constants.UPPER_FIELD_BOTTOM);
        int lowerLines = countFullRows(board, Constants.LOWER_FIELD_TOP, Constants.LOWER_FIELD_BOTTOM);
        boolean centerCleared = board.isRowFull(Constants.CENTER_ROW);

        if (upperLines == 0 && lowerLines == 0 && !centerCleared) {
            return CascadeStep.EMPTY;
        }

        if (upperLines > 0) {
            compactUpperTowardCenter(board);
        }
        if (lowerLines > 0) {
            compactLowerTowardCenter(board);
        }
        if (centerCleared) {
            clearCenterAndCascade(board);
        }

        return new CascadeStep(upperLines, lowerLines, centerCleared);
    }

    private static int countFullRows(Board board, int topRow, int bottomRow) {
        int count = 0;
        for (int r = topRow; r <= bottomRow; r++) {
            if (board.isRowFull(r)) {
                count++;
            }
        }
        return count;
    }

    private static void compactUpperTowardCenter(Board board) {
        List<List<Cell>> retained = new ArrayList<>();
        for (int r = Constants.UPPER_FIELD_TOP; r <= Constants.UPPER_FIELD_BOTTOM; r++) {
            if (!board.isRowFull(r)) {
                retained.add(board.rowSnapshot(r));
            }
        }
        int writeRow = Constants.UPPER_FIELD_BOTTOM;
        for (int i = retained.size() - 1; i >= 0; i--) {
            board.setRow(writeRow--, retained.get(i));
        }
        while (writeRow >= Constants.UPPER_FIELD_TOP) {
            board.clearRow(writeRow--);
        }
    }

    private static void compactLowerTowardCenter(Board board) {
        List<List<Cell>> retained = new ArrayList<>();
        for (int r = Constants.LOWER_FIELD_TOP; r <= Constants.LOWER_FIELD_BOTTOM; r++) {
            if (!board.isRowFull(r)) {
                retained.add(board.rowSnapshot(r));
            }
        }
        int writeRow = Constants.LOWER_FIELD_TOP;
        for (List<Cell> row : retained) {
            board.setRow(writeRow++, row);
        }
        while (writeRow <= Constants.LOWER_FIELD_BOTTOM) {
            board.clearRow(writeRow++);
        }
    }

    private static void clearCenterAndCascade(Board board) {
        List<Cell> oldUpperBottom = board.rowSnapshot(Constants.UPPER_FIELD_BOTTOM);
        List<Cell> oldLowerTop = board.rowSnapshot(Constants.LOWER_FIELD_TOP);

        for (int r = Constants.UPPER_FIELD_BOTTOM; r > Constants.UPPER_FIELD_TOP; r--) {
            board.setRow(r, board.rowSnapshot(r - 1));
        }
        board.clearRow(Constants.UPPER_FIELD_TOP);

        for (int r = Constants.LOWER_FIELD_TOP; r < Constants.LOWER_FIELD_BOTTOM; r++) {
            board.setRow(r, board.rowSnapshot(r + 1));
        }
        board.clearRow(Constants.LOWER_FIELD_BOTTOM);

        List<Cell> mergedCenter = new ArrayList<>(Constants.BOARD_WIDTH);
        for (int c = 0; c < Constants.BOARD_WIDTH; c++) {
            Cell upper = oldUpperBottom.get(c);
            Cell lower = oldLowerTop.get(c);
            mergedCenter.add(upper.isEmpty() ? lower : upper);
        }
        board.setRow(Constants.CENTER_ROW, mergedCenter);
    }
}
