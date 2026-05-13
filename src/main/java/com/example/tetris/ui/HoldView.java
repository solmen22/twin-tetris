package com.example.tetris.ui;

import com.example.tetris.domain.TetrominoType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public final class HoldView extends VBox {

    private static final double BOX_WIDTH = PieceMiniRender.DEFAULT_CELL_SIZE * PieceMiniRender.PREVIEW_BOX_CELLS;
    private static final double BOX_HEIGHT = PieceMiniRender.DEFAULT_CELL_SIZE * PieceMiniRender.PREVIEW_BOX_CELLS;

    private final Canvas canvas;
    private final Label heading;

    public HoldView() {
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(8));
        setSpacing(6);

        heading = new Label("HOLD");
        heading.setFont(Font.font(UIConstants.HUD_FONT_FAMILY, 16));
        heading.setStyle("-fx-text-fill: " + UIConstants.cssHex(UIConstants.HUD_TEXT_COLOR));

        canvas = new Canvas(BOX_WIDTH, BOX_HEIGHT);
        getChildren().addAll(heading, canvas);
    }

    public void update(TetrominoType heldType) {
        PieceMiniRender.renderPiece(
            canvas.getGraphicsContext2D(),
            heldType,
            BOX_WIDTH,
            BOX_HEIGHT,
            PieceMiniRender.DEFAULT_CELL_SIZE
        );
    }
}
