package com.example.tetris;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App extends Application {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    private static final String WINDOW_TITLE = "Bidirectional Tetris";
    private static final double WINDOW_WIDTH = 800;
    private static final double WINDOW_HEIGHT = 900;

    @Override
    public void start(Stage stage) {
        log.info("Starting {}", WINDOW_TITLE);
        StackPane root = new StackPane();
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setTitle(WINDOW_TITLE);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
