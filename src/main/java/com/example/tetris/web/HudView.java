package com.example.tetris.web;

import com.example.tetris.domain.Direction;
import com.example.tetris.domain.Score;
import com.example.tetris.game.GameMode;
import com.example.tetris.game.GameState;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

public final class HudView {

    private final HTMLElement scoreText;
    private final HTMLElement levelText;
    private final HTMLElement linesText;
    private final HTMLElement modeText;
    private final HTMLElement directionText;
    private final HTMLCanvasElement holdCanvas;
    private final HTMLCanvasElement nextCanvas;

    public HudView(HTMLDocument doc) {
        this.scoreText = doc.getElementById("score-text");
        this.levelText = doc.getElementById("level-text");
        this.linesText = doc.getElementById("lines-text");
        this.modeText = doc.getElementById("mode-text");
        this.directionText = doc.getElementById("direction-text");
        this.holdCanvas = (HTMLCanvasElement) doc.getElementById("hold-canvas");
        this.nextCanvas = (HTMLCanvasElement) doc.getElementById("next-canvas");
    }

    public void render(GameState state) {
        Score s = state.score();
        scoreText.setInnerText(Long.toString(s.points()));
        levelText.setInnerText(Integer.toString(s.level()));
        linesText.setInnerText(Integer.toString(s.lines()));
        modeText.setInnerText(modeLabel(state.mode()));
        directionText.setInnerText(directionLabel(state));
        MiniRenderer.renderHold(holdCanvas, state.heldType());
        MiniRenderer.renderNext(nextCanvas, state.nextQueue());
    }

    private static String modeLabel(GameMode mode) {
        return switch (mode) {
            case RANDOM -> "RANDOM";
            case ALTERNATING -> "ALTERNATING";
            case USER_CHOICE -> "USER CHOICE";
        };
    }

    private static String directionLabel(GameState state) {
        if (state.mode() != GameMode.USER_CHOICE) {
            return "-";
        }
        Direction pending = state.pendingDirection();
        if (pending == null) {
            return "-";
        }
        return pending == Direction.DOWN ? "DOWN" : "UP";
    }
}
