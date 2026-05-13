package com.example.tetris.ui;

import com.example.tetris.domain.Board;
import com.example.tetris.domain.Cell;
import com.example.tetris.domain.Constants;
import com.example.tetris.domain.Position;
import com.example.tetris.domain.Tetromino;
import com.example.tetris.domain.TetrominoType;
import com.example.tetris.game.GameState;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public final class GameCanvas extends Canvas {

    private static final double WIDTH = Constants.BOARD_WIDTH * UIConstants.CELL_SIZE;
    private static final double HEIGHT = Constants.BOARD_HEIGHT * UIConstants.CELL_SIZE;

    public GameCanvas() {
        super(WIDTH, HEIGHT);
    }

    public void render(GameState state) {
        GraphicsContext g = getGraphicsContext2D();
        clear(g);
        drawBoard(g, state.board());
        if (state.currentPiece() != null) {
            drawPiece(g, state.currentPiece());
        }
        drawCenterBoundary(g);
        if (state.clearFlashProgress() > 0) {
            drawClearFlash(g, state.clearFlashProgress(), state.clearFlashMultiplier());
        }
        if (state.gameOver()) {
            drawGameOver(g);
        } else if (state.paused()) {
            drawPaused(g);
        }
    }

    private void drawClearFlash(GraphicsContext g, double progress, int multiplier) {
        double baseAlpha = switch (multiplier) {
            case 5 -> 0.55;
            case 2 -> 0.30;
            default -> 0.15;
        };
        Color tint = switch (multiplier) {
            case 5 -> Color.color(1.0, 0.55, 0.2);
            case 2 -> Color.color(1.0, 0.4, 0.85);
            default -> Color.color(1.0, 1.0, 1.0);
        };
        g.setFill(tint.deriveColor(0, 1, 1, baseAlpha * progress));
        g.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private void clear(GraphicsContext g) {
        g.setFill(UIConstants.BACKGROUND_COLOR);
        g.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private void drawBoard(GraphicsContext g, Board board) {
        for (int r = 0; r < board.height(); r++) {
            for (int c = 0; c < board.width(); c++) {
                Cell cell = board.cellAt(r, c);
                drawCell(g, r, c, cell);
            }
        }
        drawGrid(g, board);
    }

    private void drawCell(GraphicsContext g, int row, int col, Cell cell) {
        double x = col * UIConstants.CELL_SIZE;
        double y = row * UIConstants.CELL_SIZE;

        if (cell.isEmpty()) {
            g.setFill(row == Constants.CENTER_ROW
                ? UIConstants.CENTER_BOUNDARY_BG
                : UIConstants.EMPTY_CELL_COLOR);
            g.fillRect(x, y, UIConstants.CELL_SIZE, UIConstants.CELL_SIZE);
            return;
        }
        fillBlock(g, x, y, colorOf(cell.type()), 1.0);
    }

    private void drawPiece(GraphicsContext g, Tetromino piece) {
        Color color = colorOf(piece.type());
        for (Position p : piece.cells()) {
            double x = p.col() * UIConstants.CELL_SIZE;
            double y = p.row() * UIConstants.CELL_SIZE;
            fillBlock(g, x, y, color, 1.0);
        }
    }

    private void fillBlock(GraphicsContext g, double x, double y, Color color, double opacity) {
        g.setFill(color.deriveColor(0, 1, 1, opacity));
        g.fillRect(x + 1, y + 1, UIConstants.CELL_SIZE - 2, UIConstants.CELL_SIZE - 2);

        g.setStroke(color.brighter());
        g.setLineWidth(1.5);
        g.strokeRect(x + 1.5, y + 1.5, UIConstants.CELL_SIZE - 3, UIConstants.CELL_SIZE - 3);
    }

    private void drawGrid(GraphicsContext g, Board board) {
        g.setStroke(UIConstants.GRID_LINE_COLOR);
        g.setLineWidth(UIConstants.GRID_LINE_WIDTH);
        for (int c = 0; c <= board.width(); c++) {
            double x = c * UIConstants.CELL_SIZE;
            g.strokeLine(x, 0, x, HEIGHT);
        }
        for (int r = 0; r <= board.height(); r++) {
            double y = r * UIConstants.CELL_SIZE;
            g.strokeLine(0, y, WIDTH, y);
        }
    }

    private void drawCenterBoundary(GraphicsContext g) {
        g.setStroke(UIConstants.CENTER_BOUNDARY_LINE);
        g.setLineWidth(UIConstants.CENTER_LINE_WIDTH);
        double topY = Constants.CENTER_ROW * UIConstants.CELL_SIZE;
        double bottomY = (Constants.CENTER_ROW + 1) * UIConstants.CELL_SIZE;
        g.strokeLine(0, topY, WIDTH, topY);
        g.strokeLine(0, bottomY, WIDTH, bottomY);
    }

    private void drawGameOver(GraphicsContext g) {
        g.setFill(Color.color(0, 0, 0, 0.55));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setFill(UIConstants.GAME_OVER_COLOR);
        g.setFont(Font.font(UIConstants.HUD_FONT_FAMILY, UIConstants.HUD_FONT_SIZE_GAME_OVER));
        g.setTextAlign(TextAlignment.CENTER);
        g.fillText("GAME OVER", WIDTH / 2.0, HEIGHT / 2.0 - 20);

        g.setFont(Font.font(UIConstants.HUD_FONT_FAMILY, UIConstants.HUD_FONT_SIZE_LARGE));
        g.setFill(UIConstants.HUD_TEXT_COLOR);
        g.fillText("R: Restart", WIDTH / 2.0, HEIGHT / 2.0 + 30);
    }

    private void drawPaused(GraphicsContext g) {
        g.setFill(Color.color(0, 0, 0, 0.45));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setFill(UIConstants.HUD_TEXT_COLOR);
        g.setFont(Font.font(UIConstants.HUD_FONT_FAMILY, UIConstants.HUD_FONT_SIZE_GAME_OVER));
        g.setTextAlign(TextAlignment.CENTER);
        g.fillText("PAUSED", WIDTH / 2.0, HEIGHT / 2.0);
    }

    private static Color colorOf(TetrominoType type) {
        return Color.rgb(type.red(), type.green(), type.blue());
    }
}
