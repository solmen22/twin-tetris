package com.example.tetris.web;

import com.example.tetris.domain.Board;
import com.example.tetris.domain.Cell;
import com.example.tetris.domain.Constants;
import com.example.tetris.domain.Position;
import com.example.tetris.domain.Tetromino;
import com.example.tetris.domain.TetrominoType;
import com.example.tetris.game.CollisionDetector;
import com.example.tetris.game.GameState;
import org.teavm.jso.JSBody;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;

public final class CanvasRenderer {

    private final CanvasRenderingContext2D g;
    private final int width;
    private final int height;

    private boolean ghostEnabled = true;
    private boolean reducedMotion = false;

    public CanvasRenderer(HTMLCanvasElement canvas) {
        // 論理サイズ(描画はこの座標系で行う)。
        this.width = Constants.BOARD_WIDTH * WebConstants.CELL_SIZE;
        this.height = Constants.BOARD_HEIGHT * WebConstants.CELL_SIZE;

        // HiDPI 対応: バッキングストアを devicePixelRatio 倍にし、CSS 表示サイズは
        // スタイルシートに委ねる(高解像度のまま縮小されてくっきり描画される)。
        double dpr = devicePixelRatio();
        if (dpr < 1.0) {
            dpr = 1.0;
        }
        canvas.setWidth((int) Math.round(width * dpr));
        canvas.setHeight((int) Math.round(height * dpr));
        this.g = (CanvasRenderingContext2D) canvas.getContext("2d");
        g.scale(dpr, dpr);
    }

    @JSBody(script = "return window.devicePixelRatio || 1;")
    private static native double devicePixelRatio();

    public void setGhostEnabled(boolean enabled) {
        this.ghostEnabled = enabled;
    }

    public void setReducedMotion(boolean reduced) {
        this.reducedMotion = reduced;
    }

    public void render(GameState state) {
        clear();
        drawBoard(state.board());
        if (state.currentPiece() != null) {
            if (ghostEnabled) {
                drawGhost(state.board(), state.currentPiece());
            }
            drawPiece(state.currentPiece());
        }
        drawCenterBoundary();
        if (state.clearFlashProgress() > 0 && !reducedMotion) {
            drawClearFlash(state.clearFlashProgress(), state.clearFlashMultiplier());
        }
        // GAME OVER / PAUSED の表示は DOM オーバーレイ側に一本化(二重描画を避ける)。
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
        g.setGlobalAlpha(WebConstants.FALLING_PIECE_ALPHA);
        for (Position p : piece.cells()) {
            double x = p.col() * WebConstants.CELL_SIZE;
            double y = p.row() * WebConstants.CELL_SIZE;
            fillBlock(x, y, piece.type());
        }
        g.setGlobalAlpha(1.0);
    }

    /** ハードドロップ時の着地位置を計算し、半透明のゴーストとして描画する。 */
    private void drawGhost(Board board, Tetromino piece) {
        int step = piece.direction().rowStep();
        Tetromino ghost = piece;
        while (true) {
            Tetromino moved = ghost.translated(step, 0);
            if (CollisionDetector.collides(board, moved)) {
                break;
            }
            ghost = moved;
        }
        if (ghost.origin().equals(piece.origin())) {
            return; // 既に接地している場合はゴースト不要
        }
        String fill = ColorUtil.rgb(piece.type());
        String stroke = ColorUtil.rgbBrighter(piece.type());
        for (Position p : ghost.cells()) {
            double x = p.col() * WebConstants.CELL_SIZE;
            double y = p.row() * WebConstants.CELL_SIZE;
            g.setGlobalAlpha(WebConstants.GHOST_ALPHA);
            g.setFillStyle(fill);
            g.fillRect(x + 1, y + 1, WebConstants.CELL_SIZE - 2, WebConstants.CELL_SIZE - 2);
            g.setGlobalAlpha(Math.min(1.0, WebConstants.GHOST_ALPHA + 0.35));
            g.setStrokeStyle(stroke);
            g.setLineWidth(1.0);
            g.strokeRect(x + 1.5, y + 1.5, WebConstants.CELL_SIZE - 3, WebConstants.CELL_SIZE - 3);
        }
        g.setGlobalAlpha(1.0);
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

    private void line(double x1, double y1, double x2, double y2) {
        g.beginPath();
        g.moveTo(x1, y1);
        g.lineTo(x2, y2);
        g.stroke();
    }
}
