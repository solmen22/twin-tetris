package com.example.tetris.web;

import com.example.tetris.game.GameEngine;
import org.teavm.jso.dom.events.KeyboardEvent;

import java.util.function.Supplier;

public final class WebKeyboardController {

    private final Supplier<GameEngine> engineSupplier;
    private final Runnable restartHandler;
    private final SettingsStore settings;

    public WebKeyboardController(
        Supplier<GameEngine> engineSupplier,
        Runnable restartHandler,
        SettingsStore settings
    ) {
        this.engineSupplier = engineSupplier;
        this.restartHandler = restartHandler;
        this.settings = settings;
    }

    public boolean handle(KeyboardEvent event) {
        String code = event.getCode();
        if (code == null || code.isEmpty()) {
            return false;
        }
        Action action = settings.actionForCode(code);
        if (action == null) {
            return false;
        }
        if (action == Action.RESET) {
            restartHandler.run();
            return true;
        }
        GameEngine engine = engineSupplier.get();
        if (engine == null) {
            return false;
        }
        switch (action) {
            case MOVE_LEFT -> engine.moveLeft();
            case MOVE_RIGHT -> engine.moveRight();
            case SOFT_DROP -> engine.softDrop();
            case HARD_DROP -> engine.hardDrop();
            case ROTATE_CW -> engine.rotateCw();
            case ROTATE_CCW -> engine.rotateCcw();
            case HOLD -> engine.hold();
            case SELECT_DOWN -> engine.selectDirectionDown();
            case SELECT_UP -> engine.selectDirectionUp();
            case PAUSE -> engine.togglePause();
            case RESET -> { /* handled above */ }
        }
        return true;
    }
}
