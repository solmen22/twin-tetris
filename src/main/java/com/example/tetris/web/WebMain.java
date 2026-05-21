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

import java.util.List;

public final class WebMain {

    private static WebMain instance;

    private final HTMLDocument document;
    private final HTMLElement menuScreen;
    private final HTMLElement gameScreen;
    private final HTMLElement gameOverOverlay;
    private final HTMLElement tutorialScreen;
    private final HTMLElement pauseOverlay;
    private final HTMLElement keyhelpList;
    private final HTMLElement finalStatsList;
    private final HTMLElement newRecordBanner;
    private final CanvasRenderer renderer;
    private final HudView hud;
    private final WebKeyboardController keyboardController;
    private final SettingsStore settings;
    private final SettingsView settingsView;
    private final TouchController touchController;

    private GameEngine engine;
    private GameMode currentMode;
    private double lastTimestamp = -1.0;
    private boolean running = false;
    private boolean gameOverShown = false;
    private boolean pauseOverlayShown = false;
    private int selectedMenuIndex = 0;

    private static final GameMode[] MENU_MODES = {
        GameMode.RANDOM, GameMode.ALTERNATING, GameMode.USER_CHOICE
    };
    private static final String[] MENU_MODE_BUTTON_IDS = {
        "mode-random", "mode-alternating", "mode-user-choice"
    };

    private WebMain() {
        this.document = Window.current().getDocument();
        this.menuScreen = document.getElementById("menu");
        this.gameScreen = document.getElementById("game");
        this.gameOverOverlay = document.getElementById("gameover");
        this.tutorialScreen = document.getElementById("tutorial");
        this.pauseOverlay = document.getElementById("pause-overlay");
        this.keyhelpList = document.getElementById("keyhelp-list");
        this.finalStatsList = document.getElementById("final-stats");
        this.newRecordBanner = document.getElementById("new-record");

        HTMLCanvasElement boardCanvas = (HTMLCanvasElement) document.getElementById("board-canvas");
        this.renderer = new CanvasRenderer(boardCanvas);
        this.hud = new HudView(document);
        this.settings = new SettingsStore();
        this.settingsView = new SettingsView(document, settings, this::showMenu);
        this.keyboardController = new WebKeyboardController(() -> engine, this::restartGame, settings);
        this.touchController = new TouchController(document, () -> engine, this::restartGame);

        wireMenu();
        wireGameOverButtons();
        wireTutorialButtons();
        wirePauseButtons();
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

        HTMLElement settingsBtn = document.getElementById("open-settings");
        if (settingsBtn != null) {
            settingsBtn.addEventListener("click", (EventListener<Event>) e -> showSettings());
        }
        HTMLElement tutorialBtn = document.getElementById("open-tutorial");
        if (tutorialBtn != null) {
            tutorialBtn.addEventListener("click", (EventListener<Event>) e -> showTutorial());
        }
    }

    private void wireTutorialButtons() {
        HTMLElement back = document.getElementById("tutorial-back");
        if (back != null) {
            back.addEventListener("click", (EventListener<Event>) e -> showMenu());
        }
    }

    private void wirePauseButtons() {
        HTMLElement resume = document.getElementById("pause-resume");
        HTMLElement restart = document.getElementById("pause-restart");
        HTMLElement toMenu = document.getElementById("pause-to-menu");
        if (resume != null) {
            resume.addEventListener("click", (EventListener<Event>) e -> {
                if (engine != null) {
                    engine.togglePause();
                }
            });
        }
        if (restart != null) {
            restart.addEventListener("click", (EventListener<Event>) e -> restartGame());
        }
        if (toMenu != null) {
            toMenu.addEventListener("click", (EventListener<Event>) e -> showMenu());
        }
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
            if (settingsView.isCapturing() || settingsView.isVisible()) {
                return;
            }
            if (isMenuVisible()) {
                handleMenuKey(event);
                return;
            }
            if (gameOverShown) {
                Action a = settings.actionForCode(event.getCode());
                if (a == Action.RESET) {
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

    private boolean isMenuVisible() {
        if (menuScreen == null) return false;
        String cls = menuScreen.getClassName();
        return cls == null || !(" " + cls + " ").contains(" hidden ");
    }

    private void handleMenuKey(KeyboardEvent event) {
        String code = event.getCode();
        if ("ArrowDown".equals(code)) {
            event.preventDefault();
            selectedMenuIndex = (selectedMenuIndex + 1) % MENU_MODES.length;
            updateMenuSelection();
        } else if ("ArrowUp".equals(code)) {
            event.preventDefault();
            selectedMenuIndex = (selectedMenuIndex + MENU_MODES.length - 1) % MENU_MODES.length;
            updateMenuSelection();
        }
    }

    private void updateMenuSelection() {
        for (int i = 0; i < MENU_MODE_BUTTON_IDS.length; i++) {
            HTMLElement btn = document.getElementById(MENU_MODE_BUTTON_IDS[i]);
            if (btn == null) continue;
            if (i == selectedMenuIndex) {
                addClass(btn, "selected");
                btn.focus();
            } else {
                removeClass(btn, "selected");
            }
        }
    }

    private void refreshMenuBestScores() {
        for (int i = 0; i < MENU_MODE_BUTTON_IDS.length; i++) {
            HTMLElement btn = document.getElementById(MENU_MODE_BUTTON_IDS[i]);
            if (btn == null) continue;
            String modeKey = MENU_MODES[i].name();
            long best = settings.bestScore(modeKey);
            HTMLElement span = btn.querySelector(".mode-best");
            if (span != null) {
                span.setInnerText(best > 0L ? "Best: " + best : "Best: -");
            }
        }
    }

    private void refreshKeyhelp() {
        if (keyhelpList == null) return;
        keyhelpList.setInnerHTML("");
        appendKeyhelpRow("左右移動", Action.MOVE_LEFT, Action.MOVE_RIGHT);
        appendKeyhelpRow("ソフトドロップ", Action.SOFT_DROP);
        appendKeyhelpRow("ハードドロップ", Action.HARD_DROP);
        appendKeyhelpRow("回転 (右 / 左)", Action.ROTATE_CW, Action.ROTATE_CCW);
        appendKeyhelpRow("ホールド", Action.HOLD);
        appendKeyhelpRow("方向選択 (USER CHOICE)", Action.SELECT_DOWN, Action.SELECT_UP);
        appendKeyhelpRow("ポーズ / リスタート", Action.PAUSE, Action.RESET);
    }

    private void appendKeyhelpRow(String label, Action... actions) {
        HTMLElement li = document.createElement("li");
        StringBuilder kbds = new StringBuilder();
        boolean first = true;
        for (Action a : actions) {
            List<String> codes = settings.codesFor(a);
            if (codes == null) continue;
            for (String c : codes) {
                if (!first) kbds.append(" ");
                first = false;
                kbds.append("<kbd>")
                    .append(escapeHtml(SettingsStore.displayLabel(c)))
                    .append("</kbd>");
            }
            if (actions.length > 1 && a != actions[actions.length - 1]) {
                kbds.append(" / ");
            }
        }
        kbds.append(" ").append(escapeHtml(label));
        li.setInnerHTML(kbds.toString());
        keyhelpList.appendChild(li);
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private void showMenu() {
        running = false;
        engine = null;
        currentMode = null;
        gameOverShown = false;
        pauseOverlayShown = false;
        settingsView.hide();
        addClass(gameScreen, "hidden");
        addClass(gameOverOverlay, "hidden");
        addClass(tutorialScreen, "hidden");
        addClass(pauseOverlay, "hidden");
        removeClass(menuScreen, "hidden");
        refreshMenuBestScores();
        refreshKeyhelp();
        updateMenuSelection();
    }

    private void showSettings() {
        addClass(menuScreen, "hidden");
        addClass(gameScreen, "hidden");
        addClass(gameOverOverlay, "hidden");
        addClass(tutorialScreen, "hidden");
        addClass(pauseOverlay, "hidden");
        settingsView.show();
    }

    private void showTutorial() {
        addClass(menuScreen, "hidden");
        addClass(gameScreen, "hidden");
        addClass(gameOverOverlay, "hidden");
        addClass(pauseOverlay, "hidden");
        settingsView.hide();
        removeClass(tutorialScreen, "hidden");
    }

    private void startGame(GameMode mode) {
        currentMode = mode;
        engine = newEngine(mode);
        lastTimestamp = -1.0;
        gameOverShown = false;
        pauseOverlayShown = false;
        running = true;
        settingsView.hide();
        addClass(menuScreen, "hidden");
        addClass(gameOverOverlay, "hidden");
        addClass(tutorialScreen, "hidden");
        addClass(pauseOverlay, "hidden");
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
        updatePauseOverlay(state.paused());
        Window.requestAnimationFrame(this::frame);
    }

    private void updatePauseOverlay(boolean paused) {
        if (pauseOverlay == null) return;
        if (paused && !pauseOverlayShown) {
            removeClass(pauseOverlay, "hidden");
            pauseOverlayShown = true;
        } else if (!paused && pauseOverlayShown) {
            addClass(pauseOverlay, "hidden");
            pauseOverlayShown = false;
        }
    }

    private void handleGameOver(GameState state) {
        Score s = state.score();
        String modeKey = currentMode != null ? currentMode.name() : "DEFAULT";
        long previousBest = settings.bestScore(modeKey);
        boolean newRecord = s.points() > previousBest && s.points() > 0L;
        if (newRecord) {
            settings.recordBestScore(modeKey, s.points());
        }
        populateFinalStats(state, previousBest, newRecord);
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

    private void populateFinalStats(GameState state, long previousBest, boolean newRecord) {
        if (finalStatsList == null) {
            return;
        }
        finalStatsList.setInnerHTML("");
        Score s = state.score();
        com.example.tetris.game.GameStats stats = state.stats();
        appendStatRow("SCORE", Long.toString(s.points()), newRecord);
        appendStatRow("BEST", Long.toString(Math.max(previousBest, s.points())), false);
        appendStatRow("LEVEL", Integer.toString(s.level()), false);
        appendStatRow("LINES", Integer.toString(s.lines()), false);
        appendStatRow("PIECES", Integer.toString(stats.piecesPlaced()), false);
        appendStatRow("中央崩落", Integer.toString(stats.centerBoundaryClears()), false);
        appendStatRow("同時崩落", Integer.toString(stats.simultaneousClears()), false);
        appendStatRow("最大連鎖", Integer.toString(stats.maxChain()), false);
        appendStatRow("PPS", formatPps(stats.piecesPlaced(), stats.elapsedMs()), false);
        appendStatRow("TIME", formatDuration(stats.elapsedMs()), false);
    }

    private void appendStatRow(String label, String value, boolean highlight) {
        HTMLElement dt = document.createElement("dt");
        dt.setInnerText(label);
        HTMLElement dd = document.createElement("dd");
        dd.setInnerText(value);
        if (highlight) {
            dd.setClassName("highlight");
        }
        finalStatsList.appendChild(dt);
        finalStatsList.appendChild(dd);
    }

    private static String formatDuration(long ms) {
        if (ms < 0) ms = 0;
        long totalSeconds = ms / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (hours > 0) {
            return hours + ":" + pad2(minutes) + ":" + pad2(seconds);
        }
        return pad2(minutes) + ":" + pad2(seconds);
    }

    private static String pad2(long n) {
        return (n < 10) ? "0" + n : Long.toString(n);
    }

    private static String formatPps(int pieces, long elapsedMs) {
        if (elapsedMs <= 0 || pieces <= 0) {
            return "0.00";
        }
        double pps = pieces * 1000.0 / elapsedMs;
        long whole = (long) pps;
        long fraction = Math.round((pps - whole) * 100.0);
        if (fraction >= 100) {
            whole += 1;
            fraction -= 100;
        }
        return whole + "." + pad2(fraction);
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
