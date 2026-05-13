package com.example.tetris;

import com.example.tetris.domain.Score;
import com.example.tetris.game.BagGenerator;
import com.example.tetris.game.DirectionStrategy;
import com.example.tetris.game.GameEngine;
import com.example.tetris.game.GameMode;
import com.example.tetris.game.GameState;
import com.example.tetris.input.KeyboardController;
import com.example.tetris.persistence.HighScoreEntry;
import com.example.tetris.persistence.HighScoreRepository;
import com.example.tetris.persistence.SettingsRepository;
import com.example.tetris.ui.GameOverDialog;
import com.example.tetris.ui.MainView;
import com.example.tetris.ui.MenuScene;
import com.example.tetris.ui.UIConstants;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class App extends Application {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final String WINDOW_TITLE = "Bidirectional Tetris";
    private static final long NANOS_PER_MS = 1_000_000L;
    private static final double MAX_TICK_MS = 100.0;

    private Stage stage;
    private HighScoreRepository highScoreRepo;
    private SettingsRepository settingsRepo;
    private AnimationTimer currentTimer;

    @Override
    public void start(Stage stage) {
        log.info("Starting {}", WINDOW_TITLE);
        this.stage = stage;
        this.highScoreRepo = new HighScoreRepository();
        this.settingsRepo = new SettingsRepository();

        stage.setTitle(WINDOW_TITLE);
        stage.setResizable(false);
        showMenu();
        stage.show();
    }

    private void showMenu() {
        stopTimer();
        MenuScene menu = new MenuScene(highScoreRepo, this::startGame);
        Scene scene = new Scene(menu.root(), UIConstants.SCENE_WIDTH, UIConstants.SCENE_HEIGHT);
        scene.setFill(UIConstants.BACKGROUND_COLOR);
        stage.setScene(scene);
    }

    private void startGame(GameMode mode) {
        stopTimer();
        settingsRepo.saveLastMode(mode);

        GameEngine engine = newEngine(mode);
        MainView view = new MainView();
        Scene scene = new Scene(view, UIConstants.SCENE_WIDTH, UIConstants.SCENE_HEIGHT);
        scene.setFill(UIConstants.BACKGROUND_COLOR);

        KeyboardController controller = new KeyboardController(() -> engine, () -> startGame(mode));
        controller.attach(scene);

        stage.setScene(scene);
        startGameLoop(engine, view, mode);
    }

    private GameEngine newEngine(GameMode mode) {
        DirectionStrategy strategy = switch (mode) {
            case RANDOM -> DirectionStrategy.random();
            case ALTERNATING -> DirectionStrategy.alternating();
            case USER_CHOICE -> DirectionStrategy.userChoice();
        };
        return new GameEngine(new BagGenerator(), strategy, mode);
    }

    private void startGameLoop(GameEngine engine, MainView view, GameMode mode) {
        currentTimer = new AnimationTimer() {
            private long lastNanos = -1L;
            private boolean ended = false;

            @Override
            public void handle(long now) {
                if (ended) {
                    return;
                }
                if (lastNanos < 0) {
                    lastNanos = now;
                    return;
                }
                double deltaMs = (now - lastNanos) / (double) NANOS_PER_MS;
                lastNanos = now;
                if (deltaMs > MAX_TICK_MS) {
                    deltaMs = MAX_TICK_MS;
                }
                engine.tick(deltaMs);
                GameState state = engine.state();
                view.render(state);
                if (state.gameOver()) {
                    ended = true;
                    Platform.runLater(() -> onGameOver(state, mode));
                }
            }
        };
        currentTimer.start();
    }

    private void stopTimer() {
        if (currentTimer != null) {
            currentTimer.stop();
            currentTimer = null;
        }
    }

    private void onGameOver(GameState finalState, GameMode mode) {
        stopTimer();
        Score finalScore = finalState.score();
        boolean newRecord = finalScore.points() > 0
            && highScoreRepo.qualifies(mode, finalScore.points());
        if (newRecord) {
            highScoreRepo.add(mode, new HighScoreEntry(
                finalScore.points(),
                finalScore.level(),
                finalScore.lines(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            ));
        }
        GameOverDialog.Result result = GameOverDialog.show(stage, finalScore, newRecord);
        if (result == GameOverDialog.Result.RETRY) {
            startGame(mode);
        } else {
            showMenu();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
