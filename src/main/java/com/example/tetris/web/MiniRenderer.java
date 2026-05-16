package com.example.tetris.web;

import com.example.tetris.domain.Position;
import com.example.tetris.domain.Rotation;
import com.example.tetris.domain.TetrominoType;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;

public final class MiniRenderer {

    public static final int CELL_SIZE = 18;

    private MiniRenderer() {
    }

    public static void renderHold(HTMLCanvasElement canvas, TetrominoType type) {
        CanvasRenderingContext2D g = ctx(canvas);
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        drawBackground(g, w, h);
        if (type != null) {
            drawCentered(g, type, w, h, 0);
        }
    }

    public static void renderNext(HTMLCanvasElement canvas, java.util.List<TetrominoType> nextQueue) {
        CanvasRenderingContext2D g = ctx(canvas);
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        drawBackground(g, w, h);
        int slotHeight = h / Math.max(1, nextQueue.size());
        for (int i = 0; i < nextQueue.size(); i++) {
            drawCenteredInRect(g, nextQueue.get(i), 0, i * slotHeight, w, slotHeight);
        }
    }

    private static CanvasRenderingContext2D ctx(HTMLCanvasElement canvas) {
        return (CanvasRenderingContext2D) canvas.getContext("2d");
    }

    private static void drawBackground(CanvasRenderingContext2D g, int w, int h) {
        g.setFillStyle(WebConstants.EMPTY_CELL_COLOR);
        g.fillRect(0, 0, w, h);
        g.setStrokeStyle(WebConstants.GRID_LINE_COLOR);
        g.setLineWidth(WebConstants.GRID_LINE_WIDTH);
        g.strokeRect(0, 0, w, h);
    }

    private static void drawCentered(CanvasRenderingContext2D g, TetrominoType type, int width, int height, int offsetY) {
        drawCenteredInRect(g, type, 0, offsetY, width, height);
    }

    private static void drawCenteredInRect(CanvasRenderingContext2D g, TetrominoType type, int rectX, int rectY, int rectW, int rectH) {
        int minRow = type.minLocalRow(Rotation.SPAWN);
        int maxRow = type.maxLocalRow(Rotation.SPAWN);
        int minCol = Integer.MAX_VALUE;
        int maxCol = Integer.MIN_VALUE;
        for (Position p : type.cellsAt(Rotation.SPAWN)) {
            if (p.col() < minCol) minCol = p.col();
            if (p.col() > maxCol) maxCol = p.col();
        }
        double pieceW = (maxCol - minCol + 1) * CELL_SIZE;
        double pieceH = (maxRow - minRow + 1) * CELL_SIZE;
        double offsetX = rectX + (rectW - pieceW) / 2.0;
        double offsetY = rectY + (rectH - pieceH) / 2.0;

        String fill = ColorUtil.rgb(type);
        String stroke = ColorUtil.rgbBrighter(type);
        for (Position p : type.cellsAt(Rotation.SPAWN)) {
            double x = offsetX + (p.col() - minCol) * CELL_SIZE;
            double y = offsetY + (p.row() - minRow) * CELL_SIZE;
            g.setFillStyle(fill);
            g.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
            g.setStrokeStyle(stroke);
            g.setLineWidth(1.0);
            g.strokeRect(x + 1.5, y + 1.5, CELL_SIZE - 3, CELL_SIZE - 3);
        }
    }
}
