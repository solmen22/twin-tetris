package com.example.tetris.ui;

import com.example.tetris.domain.Score;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class GameOverDialog {

    public enum Result { RETRY, MENU }

    private GameOverDialog() {
    }

    public static Result show(Window owner, Score finalScore, boolean isNewHighScore) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("GAME OVER");
        stage.setResizable(false);

        Label heading = new Label("GAME OVER");
        heading.setFont(Font.font(UIConstants.HUD_FONT_FAMILY, FontWeight.BOLD, 32));
        heading.setStyle("-fx-text-fill: " + UIConstants.cssHex(UIConstants.GAME_OVER_COLOR));

        Label scoreLabel = label(String.format("SCORE: %,d", finalScore.points()), 20);
        Label levelLabel = label(String.format("LEVEL: %d", finalScore.level()), 16);
        Label linesLabel = label(String.format("LINES: %d", finalScore.lines()), 16);

        VBox content = new VBox(8, heading, scoreLabel, levelLabel, linesLabel);
        if (isNewHighScore) {
            Label newRecord = label("NEW HIGH SCORE!", 18);
            newRecord.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
            content.getChildren().add(newRecord);
        }
        content.setAlignment(Pos.CENTER);

        Button retry = new Button("リトライ (R)");
        Button menu = new Button("メニューへ (M)");
        retry.setPrefWidth(140);
        menu.setPrefWidth(140);
        retry.setDefaultButton(true);

        Result[] holder = new Result[] { Result.MENU };
        retry.setOnAction(e -> { holder[0] = Result.RETRY; stage.close(); });
        menu.setOnAction(e -> { holder[0] = Result.MENU; stage.close(); });

        HBox buttons = new HBox(12, retry, menu);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, content, buttons);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: " + UIConstants.cssHex(UIConstants.BACKGROUND_COLOR));

        Scene scene = new Scene(root, 360, isNewHighScore ? 320 : 280);
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case R -> retry.fire();
                case M, ESCAPE -> menu.fire();
                default -> { /* ignore */ }
            }
        });
        stage.setScene(scene);
        stage.showAndWait();
        return holder[0];
    }

    private static Label label(String text, double size) {
        Label l = new Label(text);
        l.setFont(Font.font(UIConstants.HUD_FONT_FAMILY, size));
        l.setStyle("-fx-text-fill: " + UIConstants.cssHex(UIConstants.HUD_TEXT_COLOR));
        return l;
    }
}
