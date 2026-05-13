package com.example.tetris.ui;

import com.example.tetris.game.GameMode;
import com.example.tetris.persistence.HighScoreEntry;
import com.example.tetris.persistence.HighScoreRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.function.Consumer;

public final class MenuScene {

    private static final int TOP_PREVIEW = 5;

    private final VBox root;

    public MenuScene(HighScoreRepository repository, Consumer<GameMode> onStart) {
        root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: " + UIConstants.cssHex(UIConstants.BACKGROUND_COLOR));

        Label title = new Label("Bidirectional Tetris");
        title.setFont(Font.font(UIConstants.HUD_FONT_FAMILY, FontWeight.BOLD, 36));
        title.setStyle("-fx-text-fill: " + UIConstants.cssHex(UIConstants.HUD_TEXT_COLOR));

        Label hint = new Label("モードを選択してゲーム開始");
        hint.setFont(Font.font(UIConstants.HUD_FONT_FAMILY, 16));
        hint.setStyle("-fx-text-fill: " + UIConstants.cssHex(UIConstants.HUD_TEXT_COLOR));

        HBox modes = new HBox(18);
        modes.setAlignment(Pos.CENTER);
        modes.getChildren().addAll(
            modeColumn(GameMode.RANDOM, "ランダム", "方向が完全ランダム", repository, onStart),
            modeColumn(GameMode.ALTERNATING, "交互", "DOWN→UP→DOWN…", repository, onStart),
            modeColumn(GameMode.USER_CHOICE, "ユーザー選択", "W=上から / S=下から", repository, onStart)
        );

        Label footer = new Label("操作: ← → 移動, ↑/X 回転CW, Z 回転CCW, Space ハードドロップ,\n↓ ソフトドロップ, C/Shift Hold, W/S 方向選択, Esc/P ポーズ, R リスタート");
        footer.setFont(Font.font(UIConstants.HUD_FONT_FAMILY, 12));
        footer.setStyle("-fx-text-fill: " + UIConstants.cssHex(UIConstants.HUD_TEXT_COLOR));
        footer.setWrapText(true);

        root.getChildren().addAll(title, hint, modes, spacer(), footer);
    }

    public Region root() {
        return root;
    }

    private VBox modeColumn(GameMode mode, String label, String description,
                            HighScoreRepository repository, Consumer<GameMode> onStart) {
        VBox column = new VBox(10);
        column.setAlignment(Pos.TOP_CENTER);
        column.setPadding(new Insets(12));
        column.setPrefWidth(220);
        column.setStyle(
            "-fx-background-color: " + UIConstants.cssHex(UIConstants.EMPTY_CELL_COLOR)
            + "; -fx-background-radius: 6; -fx-border-color: "
            + UIConstants.cssHex(UIConstants.GRID_LINE_COLOR)
            + "; -fx-border-radius: 6; -fx-border-width: 1;"
        );

        Button start = new Button(label);
        start.setFont(Font.font(UIConstants.HUD_FONT_FAMILY, FontWeight.BOLD, 18));
        start.setPrefWidth(180);
        start.setOnAction(e -> onStart.accept(mode));

        Label desc = makeLabel(description, 12);
        Label scoreHeading = makeLabel("HIGH SCORES", 12);
        VBox scores = new VBox(2);
        scores.setAlignment(Pos.CENTER_LEFT);
        scores.setPadding(new Insets(4, 0, 0, 0));
        List<HighScoreEntry> top = repository.top(mode);
        if (top.isEmpty()) {
            scores.getChildren().add(makeLabel("(まだ記録なし)", 12));
        } else {
            int shown = Math.min(top.size(), TOP_PREVIEW);
            for (int i = 0; i < shown; i++) {
                HighScoreEntry e = top.get(i);
                scores.getChildren().add(makeLabel(
                    String.format("%d. %,d pt  Lv%d  %d L", i + 1, e.points(), e.level(), e.lines()),
                    12
                ));
            }
        }

        column.getChildren().addAll(start, desc, scoreHeading, scores);
        return column;
    }

    private static Region spacer() {
        Region r = new Region();
        VBox.setVgrow(r, javafx.scene.layout.Priority.ALWAYS);
        return r;
    }

    private static Label makeLabel(String text, double size) {
        Label l = new Label(text);
        l.setFont(Font.font(UIConstants.HUD_FONT_FAMILY, size));
        l.setStyle("-fx-text-fill: " + UIConstants.cssHex(UIConstants.HUD_TEXT_COLOR));
        return l;
    }
}
