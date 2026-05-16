package com.example.tetris.web;

import com.example.tetris.domain.Score;
import com.example.tetris.game.BagGenerator;
import com.example.tetris.game.DirectionStrategy;
import com.example.tetris.game.GameEngine;
import com.example.tetris.game.GameMode;
import com.example.tetris.game.GameState;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

public final class WebMain {

    private static WebMain instance;

    private final HTMLDocument document;
    private final HTMLElement menuScreen;
    private final HTMLElement gameScreen;
    private final HTMLElement gameOverOverlay;
    private final HTMLElement finalScoreText;
    private final HTMLElement newRecordBanner;
    private final CanvasRenderer renderer;
    private final HudView hud;
    private final WebKeyboardController keyboardController;

    private GameEngine engine;
    private GameMode currentMode;
    private double lastTimestamp = -1.0;
    private boolean running = false;
    private boolean gameOverShown = false;
    private long bestScore = 0L;

    private WebMain() {
        this.document = Window.current().getDocument();
        this.menuScreen = document.getElementById("menu");
        this.gameScreen = document.getElementById("game");
        this.gameOverOverlay = document.getElementById("gameover");
        this.finalScoreText = document.getElementById("final-score");
        this.newRecordBanner = document.getElementById("new-record");

        HTMLCanvasElement boardCanvas = (HTMLCanvasElement) document.getElementById("board-canvas");
        this.renderer = new CanvasRenderer(boardCanvas);
        this.hud = new HudView(document);
        this.keyboardController = new WebKeyboardController(() -> engine, this::restartGame);

        wireMenu();
        wireGameOverButtons();
        wireKeyboard();
        showMenu();
    }

    public static void main(String[] args) {
        instance = new WebMain();
    }

    private void wireMenu() {
        bindModeButton("mode-random", GameMode.RANDOM);
        bindModeButton("mode-alternating", GameMode.ALTERNATING);
        bindModeButton("mode-user-choice", GameMode.USER_CHOICE);
    }

    private void bindModeButton(String id, GameMode mode) {
        HTMLElement btn = document.getElementById(id);
        if (btn == null) {
            return;
        }
        btn.addEventListener("click", (EventListener<Event>) e -> startGame(mode));
    }

    private void wireGameOverButtons() {
        HTMLElement retry = document.getElementById("retry-button");
        HTMLElement menu = document.getElementById("menu-button");
        if (retry != null) {
            retry.addEventListener("click", (EventListener<Event>) e -> restartGame());
        }
        if (menu != null) {
            menu.addEventListener("click", (EventListener<Event>) e -> showMenu());
        }
    }

    private void wireKeyboard() {
        document.addEventListener("keydown", (EventListener<KeyboardEvent>) event -> {
            if (gameOverShown) {
                if ("r".equals(event.getKey()) || "R".equals(event.getKey())) {
                    event.preventDefault();
                    restartGame();
                }
                return;
            }
            if (running && keyboardController.handle(event)) {
                event.preventDefault();
            }
        });
    }

    private void showMenu() {
        running = false;
        engine = null;
        currentMode = null;
        gameOverShown = false;
        addClass(gameScreen, "hidden");
        addClass(gameOverOverlay, "hidden");
        removeClass(menuScreen, "hidden");
    }

    private void startGame(GameMode mode) {
        currentMode = mode;
        engine = newEngine(mode);
        lastTimestamp = -1.0;
        gameOverShown = false;
        running = true;
        addClass(menuScreen, "hidden");
        addClass(gameOverOverlay, "hidden");
        removeClass(gameScreen, "hidden");
        Window.requestAnimationFrame(this::frame);
    }

    private void restartGame() {
        if (currentMode != null) {
            startGame(currentMode);
        }
    }

    private GameEngine newEngine(GameMode mode) {
        DirectionStrategy strategy = switch (mode) {
            case RANDOM -> DirectionStrategy.random();
            case ALTERNATING -> DirectionStrategy.alternating();
            case USER_CHOICE -> DirectionStrategy.userChoice();
        };
        return new GameEngine(new BagGenerator(), strategy, mode);
    }

    private void frame(double timestamp) {
        if (!running || engine == null) {
            return;
        }
        if (lastTimestamp < 0) {
            lastTimestamp = timestamp;
            Window.requestAnimationFrame(this::frame);
            return;
        }
        double deltaMs = timestamp - lastTimestamp;
        lastTimestamp = timestamp;
        if (deltaMs > WebConstants.TICK_MAX_MS) {
            deltaMs = WebConstants.TICK_MAX_MS;
        }
        engine.tick(deltaMs);
        GameState state = engine.state();
        renderer.render(state);
        hud.render(state);
        if (state.gameOver()) {
            running = false;
            handleGameOver(state);
            return;
        }
        Window.requestAnimationFrame(this::frame);
    }

    private void handleGameOver(GameState state) {
        Score s = state.score();
        finalScoreText.setInnerText(
            "Score " + s.points() + "  /  Lv " + s.level() + "  /  Lines " + s.lines()
        );
        boolean newRecord = s.points() > bestScore && s.points() > 0L;
        if (newRecord) {
            bestScore = s.points();
        }
        if (newRecordBanner != null) {
            if (newRecord) {
                removeClass(newRecordBanner, "hidden");
            } else {
                addClass(newRecordBanner, "hidden");
            }
        }
        removeClass(gameOverOverlay, "hidden");
        gameOverShown = true;
    }

    private static void addClass(HTMLElement el, String cls) {
        if (el == null) {
            return;
        }
        String current = el.getClassName();
        if (current == null || current.isEmpty()) {
            el.setClassName(cls);
        } else if (!(" " + current + " ").contains(" " + cls + " ")) {
            el.setClassName(current + " " + cls);
        }
    }

    private static void removeClass(HTMLElement el, String cls) {
        if (el == null) {
            return;
        }
        String current = el.getClassName();
        if (current == null || current.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String part : current.split(" ")) {
            if (!part.equals(cls) && !part.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(part);
            }
        }
        el.setClassName(sb.toString());
    }
}
