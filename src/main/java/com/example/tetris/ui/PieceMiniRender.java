package com.example.tetris.ui;

import com.example.tetris.domain.Position;
import com.example.tetris.domain.Rotation;
import com.example.tetris.domain.TetrominoType;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public final class PieceMiniRender {

    public static final double DEFAULT_CELL_SIZE = 18.0;
    public static final int PREVIEW_BOX_CELLS = 4;

    private PieceMiniRender() {
    }

    public static void renderEmpty(GraphicsContext g, double width, double height) {
        g.setFill(UIConstants.EMPTY_CELL_COLOR);
        g.fillRect(0, 0, width, height);
        g.setStroke(UIConstants.GRID_LINE_COLOR);
        g.setLineWidth(UIConstants.GRID_LINE_WIDTH);
        g.strokeRect(0, 0, width, height);
    }

    public static void renderPiece(GraphicsContext g, TetrominoType type, double width, double height, double cellSize) {
        renderEmpty(g, width, height);
        if (type == null) {
            return;
        }

        int minRow = type.minLocalRow(Rotation.SPAWN);
        int maxRow = type.maxLocalRow(Rotation.SPAWN);
        int minCol = Integer.MAX_VALUE;
        int maxCol = Integer.MIN_VALUE;
        for (Position p : type.cellsAt(Rotation.SPAWN)) {
            if (p.col() < minCol) minCol = p.col();
            if (p.col() > maxCol) maxCol = p.col();
        }
        double pieceWidth = (maxCol - minCol + 1) * cellSize;
        double pieceHeight = (maxRow - minRow + 1) * cellSize;
        double offsetX = (width - pieceWidth) / 2.0;
        double offsetY = (height - pieceHeight) / 2.0;

        Color color = Color.rgb(type.red(), type.green(), type.blue());
        for (Position p : type.cellsAt(Rotation.SPAWN)) {
            double x = offsetX + (p.col() - minCol) * cellSize;
            double y = offsetY + (p.row() - minRow) * cellSize;
            g.setFill(color);
            g.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
            g.setStroke(color.brighter());
            g.setLineWidth(1.0);
            g.strokeRect(x + 1.5, y + 1.5, cellSize - 3, cellSize - 3);
        }
    }
}
