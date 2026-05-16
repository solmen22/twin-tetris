package com.example.tetris.web;

import com.example.tetris.domain.Board;
import com.example.tetris.domain.Cell;
import com.example.tetris.domain.Constants;
import com.example.tetris.domain.Position;
import com.example.tetris.domain.Tetromino;
import com.example.tetris.domain.TetrominoType;
import com.example.tetris.game.GameState;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;

public final class CanvasRenderer {

    private final HTMLCanvasElement canvas;
    private final CanvasRenderingContext2D g;
    private final int width;
    private final int height;

    public CanvasRenderer(HTMLCanvasElement canvas) {
        this.canvas = canvas;
        canvas.setWidth(Constants.BOARD_WIDTH * WebConstants.CELL_SIZE);
        canvas.setHeight(Constants.BOARD_HEIGHT * WebConstants.CELL_SIZE);
        this.width = canvas.getWidth();
        this.height = canvas.getHeight();
        this.g = (CanvasRenderingContext2D) canvas.getContext("2d");
    }

    public void render(GameState state) {
        clear();
        drawBoard(state.board());
        if (state.currentPiece() != null) {
            drawPiece(state.currentPiece());
        }
        drawCenterBoundary();
        if (state.clearFlashProgress() > 0) {
            drawClearFlash(state.clearFlashProgress(), state.clearFlashMultiplier());
        }
        if (state.gameOver()) {
            drawOverlay("GAME OVER", WebConstants.GAME_OVER_COLOR, "R / Retry");
        } else if (state.paused()) {
            drawOverlay("PAUSED", WebConstants.HUD_TEXT_COLOR, null);
        }
    }

    private void clear() {
        g.setFillStyle(WebConstants.BACKGROUND_COLOR);
        g.fillRect(0, 0, width, height);
    }

    private void drawBoard(Board board) {
        for (int r = 0; r < board.height(); r++) {
            for (int c = 0; c < board.width(); c++) {
                Cell cell = board.cellAt(r, c);
                drawCell(r, c, cell);
            }
        }
        drawGrid(board);
    }

    private void drawCell(int row, int col, Cell cell) {
        double x = col * WebConstants.CELL_SIZE;
        double y = row * WebConstants.CELL_SIZE;

        if (cell.isEmpty()) {
            g.setFillStyle(row == Constants.CENTER_ROW
                ? WebConstants.CENTER_BOUNDARY_BG
                : WebConstants.EMPTY_CELL_COLOR);
            g.fillRect(x, y, WebConstants.CELL_SIZE, WebConstants.CELL_SIZE);
            return;
        }
        fillBlock(x, y, cell.type());
    }

    private void drawPiece(Tetromino piece) {
        for (Position p : piece.cells()) {
            double x = p.col() * WebConstants.CELL_SIZE;
            double y = p.row() * WebConstants.CELL_SIZE;
            fillBlock(x, y, piece.type());
        }
    }

    private void fillBlock(double x, double y, TetrominoType type) {
        String fill = ColorUtil.rgb(type);
        String stroke = ColorUtil.rgbBrighter(type);

        g.setFillStyle(fill);
        g.fillRect(x + 1, y + 1, WebConstants.CELL_SIZE - 2, WebConstants.CELL_SIZE - 2);

        g.setStrokeStyle(stroke);
        g.setLineWidth(1.5);
        g.strokeRect(x + 1.5, y + 1.5, WebConstants.CELL_SIZE - 3, WebConstants.CELL_SIZE - 3);
    }

    private void drawGrid(Board board) {
        g.setStrokeStyle(WebConstants.GRID_LINE_COLOR);
        g.setLineWidth(WebConstants.GRID_LINE_WIDTH);
        for (int c = 0; c <= board.width(); c++) {
            double x = c * WebConstants.CELL_SIZE;
            line(x, 0, x, height);
        }
        for (int r = 0; r <= board.height(); r++) {
            double y = r * WebConstants.CELL_SIZE;
            line(0, y, width, y);
        }
    }

    private void drawCenterBoundary() {
        g.setStrokeStyle(WebConstants.CENTER_BOUNDARY_LINE);
        g.setLineWidth(WebConstants.CENTER_LINE_WIDTH);
        double topY = Constants.CENTER_ROW * WebConstants.CELL_SIZE;
        double bottomY = (Constants.CENTER_ROW + 1) * WebConstants.CELL_SIZE;
        line(0, topY, width, topY);
        line(0, bottomY, width, bottomY);
    }

    private void drawClearFlash(double progress, int multiplier) {
        double baseAlpha = switch (multiplier) {
            case 5 -> 0.55;
            case 2 -> 0.30;
            default -> 0.15;
        };
        String tint = switch (multiplier) {
            case 5 -> "255, 140, 51";
            case 2 -> "255, 102, 217";
            default -> "255, 255, 255";
        };
        g.setFillStyle("rgba(" + tint + ", " + (baseAlpha * progress) + ")");
        g.fillRect(0, 0, width, height);
    }

    private void drawOverlay(String text, String textColor, String subtitle) {
        g.setFillStyle("rgba(0, 0, 0, 0.55)");
        g.fillRect(0, 0, width, height);

        g.setFillStyle(textColor);
        g.setFont("bold 36px " + WebConstants.HUD_FONT);
        g.setTextAlign("center");
        g.setTextBaseline("middle");
        g.fillText(text, width / 2.0, height / 2.0 - (subtitle != null ? 20 : 0));

        if (subtitle != null) {
            g.setFillStyle(WebConstants.HUD_TEXT_COLOR);
            g.setFont("18px " + WebConstants.HUD_FONT);
            g.fillText(subtitle, width / 2.0, height / 2.0 + 24);
        }
    }

    private void line(double x1, double y1, double x2, double y2) {
        g.beginPath();
        g.moveTo(x1, y1);
        g.lineTo(x2, y2);
        g.stroke();
    }
}
