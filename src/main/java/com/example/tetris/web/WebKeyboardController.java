package com.example.tetris.web;

import com.example.tetris.game.GameEngine;
import org.teavm.jso.dom.events.KeyboardEvent;

import java.util.function.Supplier;

public final class WebKeyboardController {

    private final Supplier<GameEngine> engineSupplier;
    private final Runnable restartHandler;

    public WebKeyboardController(Supplier<GameEngine> engineSupplier, Runnable restartHandler) {
        this.engineSupplier = engineSupplier;
        this.restartHandler = restartHandler;
    }

    public boolean handle(KeyboardEvent event) {
        String key = event.getKey();
        if (key == null) {
            return false;
        }
        GameEngine engine = engineSupplier.get();
        if (engine == null) {
            return false;
        }

        if (matches(key, "r", "R")) {
            restartHandler.run();
            return true;
        }
        if (matches(key, "Escape", "p", "P")) {
            engine.togglePause();
            return true;
        }
        if (matches(key, "ArrowLeft")) {
            engine.moveLeft();
            return true;
        }
        if (matches(key, "ArrowRight")) {
            engine.moveRight();
            return true;
        }
        if (matches(key, "ArrowDown")) {
            engine.softDrop();
            return true;
        }
        if (matches(key, " ", "Spacebar")) {
            engine.hardDrop();
            return true;
        }
        if (matches(key, "ArrowUp", "x", "X")) {
            engine.rotateCw();
            return true;
        }
        if (matches(key, "z", "Z")) {
            engine.rotateCcw();
            return true;
        }
        if (matches(key, "c", "C", "Shift")) {
            engine.hold();
            return true;
        }
        if (matches(key, "w", "W")) {
            engine.selectDirectionDown();
            return true;
        }
        if (matches(key, "s", "S")) {
            engine.selectDirectionUp();
            return true;
        }
        return false;
    }

    private static boolean matches(String key, String... candidates) {
        for (String c : candidates) {
            if (c.equals(key)) {
                return true;
            }
        }
        return false;
    }
}
