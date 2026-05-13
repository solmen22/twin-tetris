package com.example.tetris;

import com.example.tetris.game.GameEngine;
import com.example.tetris.game.RandomPieceProvider;
import com.example.tetris.input.KeyboardController;
import com.example.tetris.ui.MainView;
import com.example.tetris.ui.UIConstants;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App extends Application {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final String WINDOW_TITLE = "Bidirectional Tetris";
    private static final long NANOS_PER_MS = 1_000_000L;
    private static final double MAX_TICK_MS = 100.0;

    private GameEngine engine;
    private MainView mainView;

    @Override
    public void start(Stage stage) {
        log.info("Starting {}", WINDOW_TITLE);
        engine = newEngine();
        mainView = new MainView();

        Scene scene = new Scene(mainView, UIConstants.SCENE_WIDTH, UIConstants.SCENE_HEIGHT);
        scene.setFill(UIConstants.BACKGROUND_COLOR);

        KeyboardController controller = new KeyboardController(() -> engine, this::restart);
        controller.attach(scene);

        stage.setTitle(WINDOW_TITLE);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        startGameLoop();
    }

    private GameEngine newEngine() {
        return new GameEngine(new RandomPieceProvider());
    }

    private void restart() {
        log.info("Restarting game");
        engine = newEngine();
    }

    private void startGameLoop() {
        new AnimationTimer() {
            private long lastNanos = -1L;

            @Override
            public void handle(long now) {
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
                mainView.render(engine.state());
            }
        }.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
