package com.example.tetris.ui;

import com.example.tetris.game.GameState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class MainView extends VBox {

    private final ScorePanel scorePanel;
    private final HoldView holdView;
    private final NextQueueView nextQueueView;
    private final GameCanvas gameCanvas;
    private final ModePanel modePanel;

    public MainView() {
        super();
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: " + UIConstants.cssHex(UIConstants.BACKGROUND_COLOR));

        scorePanel = new ScorePanel();
        holdView = new HoldView();
        nextQueueView = new NextQueueView();
        gameCanvas = new GameCanvas();
        modePanel = new ModePanel();

        HBox playRow = new HBox(UIConstants.BOARD_PADDING, holdView, gameCanvas, nextQueueView);
        playRow.setAlignment(Pos.TOP_CENTER);
        playRow.setPadding(new Insets(UIConstants.BOARD_PADDING));

        getChildren().addAll(scorePanel, playRow, modePanel);
    }

    public void render(GameState state) {
        scorePanel.update(state.score());
        holdView.update(state.heldType());
        nextQueueView.update(state.nextQueue());
        gameCanvas.render(state);
        modePanel.update(state);
    }

    public GameCanvas gameCanvas() {
        return gameCanvas;
    }
}
