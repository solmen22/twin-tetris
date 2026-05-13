package com.example.tetris.ui;

import com.example.tetris.game.GameState;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class MainView extends VBox {

    private final ScorePanel scorePanel;
    private final GameCanvas gameCanvas;

    public MainView() {
        super();
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: #101020");

        scorePanel = new ScorePanel();
        gameCanvas = new GameCanvas();

        StackPane canvasHolder = new StackPane(gameCanvas);
        canvasHolder.setAlignment(Pos.CENTER);
        canvasHolder.setPadding(new javafx.geometry.Insets(UIConstants.BOARD_PADDING));

        getChildren().addAll(scorePanel, canvasHolder);
    }

    public void render(GameState state) {
        scorePanel.update(state.score());
        gameCanvas.render(state);
    }

    public GameCanvas gameCanvas() {
        return gameCanvas;
    }
}
