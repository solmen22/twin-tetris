package com.example.tetris.ui;

import com.example.tetris.domain.TetrominoType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

public final class NextQueueView extends VBox {

    private static final double BOX_WIDTH = PieceMiniRender.DEFAULT_CELL_SIZE * PieceMiniRender.PREVIEW_BOX_CELLS;
    private static final double BOX_HEIGHT = PieceMiniRender.DEFAULT_CELL_SIZE * PieceMiniRender.PREVIEW_BOX_CELLS;
    private static final int PREVIEW_COUNT = 3;

    private final List<Canvas> slots;

    public NextQueueView() {
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(8));
        setSpacing(6);

        Label heading = new Label("NEXT");
        heading.setFont(Font.font(UIConstants.HUD_FONT_FAMILY, 16));
        heading.setStyle("-fx-text-fill: " + UIConstants.cssHex(UIConstants.HUD_TEXT_COLOR));

        slots = new ArrayList<>(PREVIEW_COUNT);
        getChildren().add(heading);
        for (int i = 0; i < PREVIEW_COUNT; i++) {
            Canvas canvas = new Canvas(BOX_WIDTH, BOX_HEIGHT);
            slots.add(canvas);
            getChildren().add(canvas);
        }
    }

    public void update(List<TetrominoType> queue) {
        for (int i = 0; i < slots.size(); i++) {
            TetrominoType type = i < queue.size() ? queue.get(i) : null;
            PieceMiniRender.renderPiece(
                slots.get(i).getGraphicsContext2D(),
                type,
                BOX_WIDTH,
                BOX_HEIGHT,
                PieceMiniRender.DEFAULT_CELL_SIZE
            );
        }
    }
}
