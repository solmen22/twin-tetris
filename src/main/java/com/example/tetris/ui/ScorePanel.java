package com.example.tetris.ui;

import com.example.tetris.domain.Score;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

public final class ScorePanel extends HBox {

    private final Label scoreLabel;
    private final Label levelLabel;
    private final Label linesLabel;

    public ScorePanel() {
        super(40);
        setAlignment(Pos.CENTER);
        setPadding(new javafx.geometry.Insets(10));
        setStyle("-fx-background-color: " + toCssHex(UIConstants.BACKGROUND_COLOR));

        scoreLabel = makeLabel("SCORE: 0");
        levelLabel = makeLabel("LEVEL: 1");
        linesLabel = makeLabel("LINES: 0");

        getChildren().addAll(scoreLabel, levelLabel, linesLabel);
    }

    public void update(Score score) {
        scoreLabel.setText(String.format("SCORE: %d", score.points()));
        levelLabel.setText(String.format("LEVEL: %d", score.level()));
        linesLabel.setText(String.format("LINES: %d", score.lines()));
    }

    private static Label makeLabel(String initial) {
        Label l = new Label(initial);
        l.setFont(Font.font(UIConstants.HUD_FONT_FAMILY, UIConstants.HUD_FONT_SIZE_LARGE));
        l.setStyle("-fx-text-fill: " + toCssHex(UIConstants.HUD_TEXT_COLOR));
        return l;
    }

    private static String toCssHex(javafx.scene.paint.Color color) {
        return String.format("#%02x%02x%02x",
            (int) Math.round(color.getRed() * 255),
            (int) Math.round(color.getGreen() * 255),
            (int) Math.round(color.getBlue() * 255));
    }
}
