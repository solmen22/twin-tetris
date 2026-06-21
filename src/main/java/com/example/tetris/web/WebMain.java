package com.example.tetris.web;

import com.example.tetris.domain.Score;
import com.example.tetris.game.BagGenerator;
import com.example.tetris.game.DirectionStrategy;
import com.example.tetris.game.GameEngine;
import com.example.tetris.game.GameMode;
import com.example.tetris.game.GameState;
import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLInputElement;

import java.util.List;

public final class WebMain {

    private static WebMain instance;

    private final HTMLDocument document;
    private final HTMLElement menuScreen;
    private final HTMLElement gameScreen;
    private final HTMLElement gameOverOverlay;
    private final HTMLElement tutorialScreen;
    private final HTMLElement highscoresScreen;
    private final HTMLElement highscoresBody;
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
    private final SoundManager sound;

    private GameEngine engine;
    private GameMode currentMode;
    private double lastTimestamp = -1.0;
    private boolean running = false;
    private boolean gameOverShown = false;
    private boolean pauseOverlayShown = false;
    private int selectedMenuIndex = 0;

    // 効果音をフレーム差分で検出するための前フレーム値。
    private int prevPieces = 0;
    private int prevLines = 0;
    private int prevLevel = 1;
    private boolean prevFlashActive = false;

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
        this.highscoresScreen = document.getElementById("highscores");
        this.highscoresBody = document.getElementById("highscores-body");
        this.pauseOverlay = document.getElementById("pause-overlay");
        this.keyhelpList = document.getElementById("keyhelp-list");
        this.finalStatsList = document.getElementById("final-stats");
        this.newRecordBanner = document.getElementById("new-record");

        HTMLCanvasElement boardCanvas = (HTMLCanvasElement) document.getElementById("board-canvas");
        this.renderer = new CanvasRenderer(boardCanvas);
        this.hud = new HudView(document);
        this.settings = new SettingsStore();
        this.sound = new SoundManager(settings);
        this.settingsView = new SettingsView(document, settings, this::showMenu, sound, renderer);
        this.keyboardController = new WebKeyboardController(() -> engine, this::restartGame, settings, sound);
        this.touchController = new TouchController(document, () -> engine, this::restartGame, sound);

        wireMenu();
        wireGameOverButtons();
        wireTutorialButtons();
        wireHighScoresButtons();
        wirePauseButtons();
        wireKeyboard();
        wireWindowEvents();
        showMenu();
        maybeShowFirstRunHint();
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
        HTMLElement highscoresBtn = document.getElementById("open-highscores");
        if (highscoresBtn != null) {
            highscoresBtn.addEventListener("click", (EventListener<Event>) e -> showHighScores());
        }
        wireUsernameField();
    }

    private void wireHighScoresButtons() {
        HTMLElement back = document.getElementById("highscores-back");
        if (back != null) {
            back.addEventListener("click", (EventListener<Event>) e -> showMenu());
        }
        HTMLElement clear = document.getElementById("highscores-clear");
        if (clear != null) {
            clear.addEventListener("click", (EventListener<Event>) e -> {
                settings.clearHighScores();
                populateHighScores();
                refreshMenuBestScores();
            });
        }
    }

    private void wireUsernameField() {
        HTMLInputElement input = (HTMLInputElement) document.getElementById("username-input");
        HTMLElement status = document.getElementById("username-status");
        if (input == null) return;
        input.setValue(settings.username());
        updateUsernameStatus(status, input.getValue());
        input.addEventListener("input", (EventListener<Event>) e -> {
            String raw = input.getValue();
            String sanitized = SettingsStore.sanitizeUsername(raw);
            if (!raw.equals(sanitized)) {
                input.setValue(sanitized);
            }
            settings.setUsername(sanitized);
            updateUsernameStatus(status, sanitized);
        });
    }

    private static void updateUsernameStatus(HTMLElement status, String value) {
        if (status == null) return;
        if (value == null || value.isEmpty()) {
            status.setInnerText("ゲストとして記録されます");
            removeClass(status, "saved");
        } else {
            status.setInnerText("「" + value + "」で保存中  (" + value.length() + " / " + SettingsStore.USERNAME_MAX_LENGTH + ")");
            addClass(status, "saved");
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
                handleGameOverKey(event);
                return;
            }
            if (running && keyboardController.onKeyDown(event)) {
                event.preventDefault();
            }
        });
        document.addEventListener("keyup", (EventListener<KeyboardEvent>) keyboardController::onKeyUp);
    }

    private void handleGameOverKey(KeyboardEvent event) {
        String code = event.getCode();
        if ("Enter".equals(code) || "Space".equals(code)) {
            event.preventDefault();
            restartGame();
            return;
        }
        if ("Escape".equals(code)) {
            event.preventDefault();
            showMenu();
            return;
        }
        if (settings.actionForCode(code) == Action.RESET) {
            event.preventDefault();
            restartGame();
        }
    }

    private void wireWindowEvents() {
        Window.current().addEventListener("blur", (EventListener<Event>) e -> autoPause());
        document.addEventListener("visibilitychange", (EventListener<Event>) e -> {
            if (isDocumentHidden()) {
                autoPause();
            }
        });
    }

    private void autoPause() {
        if (running && engine != null && !engine.state().paused() && !engine.state().gameOver()) {
            engine.togglePause();
            keyboardController.releaseAll();
        }
    }

    @JSBody(script = "return document.hidden;")
    private static native boolean isDocumentHidden();

    private boolean isMenuVisible() {
        if (menuScreen == null) return false;
        String cls = menuScreen.getClassName();
        return cls == null || !(" " + cls + " ").contains(" hidden ");
    }

    private void handleMenuKey(KeyboardEvent event) {
        HTMLElement active = document.getActiveElement();
        if (active != null) {
            String tag = active.getTagName();
            if (tag != null && ("INPUT".equalsIgnoreCase(tag) || "TEXTAREA".equalsIgnoreCase(tag))) {
                return;
            }
        }
        String code = event.getCode();
        if ("ArrowDown".equals(code)) {
            event.preventDefault();
            selectedMenuIndex = (selectedMenuIndex + 1) % MENU_MODES.length;
            updateMenuSelection();
        } else if ("ArrowUp".equals(code)) {
            event.preventDefault();
            selectedMenuIndex = (selectedMenuIndex + MENU_MODES.length - 1) % MENU_MODES.length;
            updateMenuSelection();
        } else if ("Enter".equals(code) || "Space".equals(code)) {
            event.preventDefault();
            startGame(MENU_MODES[selectedMenuIndex]);
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
        keyboardController.releaseAll();
        sound.refresh();
        settingsView.hide();
        addClass(gameScreen, "hidden");
        addClass(gameOverOverlay, "hidden");
        addClass(tutorialScreen, "hidden");
        addClass(highscoresScreen, "hidden");
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
        addClass(highscoresScreen, "hidden");
        addClass(pauseOverlay, "hidden");
        settingsView.show();
    }

    private void maybeShowFirstRunHint() {
        if (settings.isFirstVisit()) {
            HTMLElement tutorialBtn = document.getElementById("open-tutorial");
            addClass(tutorialBtn, "pulse");
        }
    }

    private void clearFirstRunHint() {
        settings.markVisited();
        HTMLElement tutorialBtn = document.getElementById("open-tutorial");
        removeClass(tutorialBtn, "pulse");
    }

    private void showTutorial() {
        clearFirstRunHint();
        addClass(menuScreen, "hidden");
        addClass(gameScreen, "hidden");
        addClass(gameOverOverlay, "hidden");
        addClass(highscoresScreen, "hidden");
        addClass(pauseOverlay, "hidden");
        settingsView.hide();
        removeClass(tutorialScreen, "hidden");
    }

    private void showHighScores() {
        addClass(menuScreen, "hidden");
        addClass(gameScreen, "hidden");
        addClass(gameOverOverlay, "hidden");
        addClass(tutorialScreen, "hidden");
        addClass(pauseOverlay, "hidden");
        settingsView.hide();
        populateHighScores();
        removeClass(highscoresScreen, "hidden");
    }

    private void populateHighScores() {
        if (highscoresBody == null) {
            return;
        }
        highscoresBody.setInnerHTML("");
        for (int i = 0; i < MENU_MODES.length; i++) {
            GameMode mode = MENU_MODES[i];
            HTMLElement section = document.createElement("section");
            section.setClassName("highscore-mode");

            HTMLElement title = document.createElement("h3");
            title.setInnerText(mode.name().replace('_', ' '));
            section.appendChild(title);

            List<SettingsStore.ScoreEntry> entries = settings.highScores(mode.name());
            if (entries.isEmpty()) {
                HTMLElement empty = document.createElement("p");
                empty.setClassName("highscore-empty");
                empty.setInnerText("まだ記録がありません");
                section.appendChild(empty);
            } else {
                section.appendChild(buildScoreTable(entries));
            }
            highscoresBody.appendChild(section);
        }
    }

    private HTMLElement buildScoreTable(List<SettingsStore.ScoreEntry> entries) {
        HTMLElement table = document.createElement("table");
        table.setClassName("highscore-table");

        HTMLElement thead = document.createElement("thead");
        HTMLElement headRow = document.createElement("tr");
        appendCell(headRow, "th", "#");
        appendCell(headRow, "th", "SCORE");
        appendCell(headRow, "th", "LV");
        appendCell(headRow, "th", "LINES");
        appendCell(headRow, "th", "NAME");
        appendCell(headRow, "th", "DATE");
        thead.appendChild(headRow);
        table.appendChild(thead);

        HTMLElement tbody = document.createElement("tbody");
        for (int i = 0; i < entries.size(); i++) {
            SettingsStore.ScoreEntry e = entries.get(i);
            HTMLElement row = document.createElement("tr");
            appendCell(row, "td", Integer.toString(i + 1));
            appendCell(row, "td", Long.toString(e.score()));
            appendCell(row, "td", Integer.toString(e.level()));
            appendCell(row, "td", Integer.toString(e.lines()));
            appendCell(row, "td", e.name() == null || e.name().isEmpty() ? "ゲスト" : e.name());
            appendCell(row, "td", formatDate(e.dateMs()));
            tbody.appendChild(row);
        }
        table.appendChild(tbody);
        return table;
    }

    private void appendCell(HTMLElement row, String tag, String text) {
        HTMLElement cell = document.createElement(tag);
        cell.setInnerText(text);
        row.appendChild(cell);
    }

    @JSBody(params = {"ms"}, script = "var d=new Date(ms);"
        + "function p(n){return (n<10?'0':'')+n;}"
        + "return d.getFullYear()+'-'+p(d.getMonth()+1)+'-'+p(d.getDate());")
    private static native String formatDate(double ms);

    private void startGame(GameMode mode) {
        clearFirstRunHint();
        currentMode = mode;
        engine = newEngine(mode);
        lastTimestamp = -1.0;
        gameOverShown = false;
        pauseOverlayShown = false;
        running = true;
        prevPieces = 0;
        prevLines = 0;
        prevLevel = 1;
        prevFlashActive = false;
        sound.unlock();
        sound.refresh();
        renderer.setGhostEnabled(settings.ghostEnabled());
        renderer.setReducedMotion(settings.reduceMotion());
        keyboardController.releaseAll();
        settingsView.hide();
        addClass(menuScreen, "hidden");
        addClass(gameOverOverlay, "hidden");
        addClass(tutorialScreen, "hidden");
        addClass(highscoresScreen, "hidden");
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
        keyboardController.update(deltaMs);
        engine.tick(deltaMs);
        GameState state = engine.state();
        renderer.render(state);
        hud.render(state);
        detectAndPlaySounds(state);
        if (state.gameOver()) {
            running = false;
            handleGameOver(state);
            return;
        }
        updatePauseOverlay(state.paused());
        Window.requestAnimationFrame(this::frame);
    }

    private void detectAndPlaySounds(GameState state) {
        com.example.tetris.game.GameStats stats = state.stats();
        int lines = state.score().lines();
        int level = state.score().level();
        int pieces = stats.piecesPlaced();
        boolean flashActive = state.clearFlashProgress() > 0;

        boolean cleared = flashActive && !prevFlashActive;
        if (cleared) {
            int delta = lines - prevLines;
            int mult = state.clearFlashMultiplier();
            if (mult >= 2) {
                sound.play("center");
            } else if (delta >= 4) {
                sound.play("tetris");
            } else {
                sound.play("lineclear", Math.max(1, delta));
            }
        }
        boolean hardDrop = keyboardController.consumeHardDropFlag();
        if (pieces > prevPieces && !cleared && !hardDrop) {
            sound.play("lock");
        }
        if (level > prevLevel) {
            sound.play("levelup");
        }
        prevPieces = pieces;
        prevLines = lines;
        prevLevel = level;
        prevFlashActive = flashActive;
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
        int rank = settings.recordHighScore(modeKey, s.points(), s.level(), s.lines(), settings.username());
        boolean newRecord = rank == 1 && s.points() > 0L;
        keyboardController.releaseAll();
        sound.play("gameover");
        populateFinalStats(state, previousBest, newRecord, rank);
        if (newRecordBanner != null) {
            if (newRecord) {
                newRecordBanner.setInnerText("★ NEW RECORD ★");
                removeClass(newRecordBanner, "hidden");
            } else if (rank > 0) {
                newRecordBanner.setInnerText("ハイスコア入り  #" + rank + " / " + SettingsStore.MAX_HIGH_SCORES);
                removeClass(newRecordBanner, "hidden");
            } else {
                addClass(newRecordBanner, "hidden");
            }
        }
        removeClass(gameOverOverlay, "hidden");
        gameOverShown = true;
    }

    private void populateFinalStats(GameState state, long previousBest, boolean newRecord, int rank) {
        if (finalStatsList == null) {
            return;
        }
        finalStatsList.setInnerHTML("");
        Score s = state.score();
        com.example.tetris.game.GameStats stats = state.stats();
        String user = settings.username();
        appendStatRow("PLAYER", user.isEmpty() ? "ゲスト" : user, false);
        appendStatRow("SCORE", Long.toString(s.points()), newRecord);
        appendStatRow("BEST", Long.toString(Math.max(previousBest, s.points())), false);
        if (rank > 0) {
            appendStatRow("RANK", "#" + rank + " / " + SettingsStore.MAX_HIGH_SCORES, rank == 1);
        }
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
