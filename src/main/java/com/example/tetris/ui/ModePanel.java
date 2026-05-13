package com.example.tetris.ui;

import com.example.tetris.domain.Direction;
import com.example.tetris.game.GameMode;
import com.example.tetris.game.GameState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public final class ModePanel extends VBox {

    private final Label modeLabel;
    private final Label directionLabel;

    public ModePanel() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(8));
        setSpacing(4);

        modeLabel = makeLabel("MODE: -");
        directionLabel = makeLabel("");
        getChildren().addAll(modeLabel, directionLabel);
    }

    public void update(GameState state) {
        modeLabel.setText("MODE: " + displayName(state.mode()));
        if (state.mode() == GameMode.USER_CHOICE) {
            String pending = state.pendingDirection() == null
                ? "?"
                : (state.pendingDirection() == Direction.DOWN ? "上から落下 (W)" : "下から上昇 (S)");
            String grace = state.inSpawnGrace() ? " [選択可]" : "";
            directionLabel.setText("次の方向: " + pending + grace);
        } else {
            directionLabel.setText("");
        }
    }

    private static String displayName(GameMode mode) {
        return switch (mode) {
            case RANDOM -> "ランダム";
            case ALTERNATING -> "交互";
            case USER_CHOICE -> "ユーザー選択";
        };
    }

    private static Label makeLabel(String initial) {
        Label l = new Label(initial);
        l.setFont(Font.font(UIConstants.HUD_FONT_FAMILY, 16));
        l.setStyle("-fx-text-fill: " + UIConstants.cssHex(UIConstants.HUD_TEXT_COLOR));
        return l;
    }
}
