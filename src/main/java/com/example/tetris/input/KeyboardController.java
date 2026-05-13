package com.example.tetris.input;

import com.example.tetris.game.GameEngine;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.function.Supplier;

public final class KeyboardController {

    private final Supplier<GameEngine> engineSupplier;
    private final Runnable restartHandler;

    public KeyboardController(Supplier<GameEngine> engineSupplier, Runnable restartHandler) {
        this.engineSupplier = engineSupplier;
        this.restartHandler = restartHandler;
    }

    public void attach(Scene scene) {
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            GameEngine engine = engineSupplier.get();

            if (KeyBindings.RESTART.contains(code)) {
                restartHandler.run();
                return;
            }
            if (KeyBindings.PAUSE.contains(code)) {
                engine.togglePause();
                return;
            }
            if (KeyBindings.MOVE_LEFT.contains(code)) {
                engine.moveLeft();
            } else if (KeyBindings.MOVE_RIGHT.contains(code)) {
                engine.moveRight();
            } else if (KeyBindings.SOFT_DROP.contains(code)) {
                engine.softDrop();
            } else if (KeyBindings.HARD_DROP.contains(code)) {
                engine.hardDrop();
            } else if (KeyBindings.ROTATE_CW.contains(code)) {
                engine.rotateCw();
            } else if (KeyBindings.ROTATE_CCW.contains(code)) {
                engine.rotateCcw();
            }
        });
    }
}
