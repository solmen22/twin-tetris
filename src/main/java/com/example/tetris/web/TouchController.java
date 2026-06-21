package com.example.tetris.web;

import com.example.tetris.game.GameEngine;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

import java.util.function.Supplier;

public final class TouchController {

    private static final int REPEAT_DELAY_MS = 180;
    private static final int REPEAT_INTERVAL_MS = 55;

    private final Supplier<GameEngine> engineSupplier;
    private final Runnable restartHandler;
    private final SoundManager sound;

    private int activeRepeatHandle = -1;
    private int activeDelayHandle = -1;

    public TouchController(
        HTMLDocument document,
        Supplier<GameEngine> engineSupplier,
        Runnable restartHandler,
        SoundManager sound
    ) {
        this.engineSupplier = engineSupplier;
        this.restartHandler = restartHandler;
        this.sound = sound;

        HTMLElement container = document.getElementById("touch-controls");
        if (container == null) {
            return;
        }
        wireButton(document, "tc-move-left", Action.MOVE_LEFT, true);
        wireButton(document, "tc-move-right", Action.MOVE_RIGHT, true);
        wireButton(document, "tc-soft-drop", Action.SOFT_DROP, true);
        wireButton(document, "tc-hard-drop", Action.HARD_DROP, false);
        wireButton(document, "tc-rotate-cw", Action.ROTATE_CW, false);
        wireButton(document, "tc-rotate-ccw", Action.ROTATE_CCW, false);
        wireButton(document, "tc-hold", Action.HOLD, false);
        wireButton(document, "tc-pause", Action.PAUSE, false);
        wireButton(document, "tc-reset", Action.RESET, false);
        wireButton(document, "tc-select-down", Action.SELECT_DOWN, false);
        wireButton(document, "tc-select-up", Action.SELECT_UP, false);
    }

    private void wireButton(HTMLDocument document, String id, Action action, boolean repeatable) {
        HTMLElement btn = document.getElementById(id);
        if (btn == null) {
            return;
        }
        btn.addEventListener("pointerdown", (EventListener<Event>) e -> {
            e.preventDefault();
            dispatch(action);
            if (repeatable) {
                scheduleRepeat(action);
            }
        });
        btn.addEventListener("pointerup", (EventListener<Event>) e -> cancelRepeat());
        btn.addEventListener("pointercancel", (EventListener<Event>) e -> cancelRepeat());
        btn.addEventListener("pointerleave", (EventListener<Event>) e -> cancelRepeat());
        btn.addEventListener("touchstart", (EventListener<Event>) e -> e.preventDefault());
        btn.addEventListener("contextmenu", (EventListener<Event>) Event::preventDefault);
    }

    private void scheduleRepeat(Action action) {
        cancelRepeat();
        activeDelayHandle = Window.setTimeout(() -> {
            activeDelayHandle = -1;
            dispatch(action);
            activeRepeatHandle = Window.setInterval(() -> dispatch(action), REPEAT_INTERVAL_MS);
        }, REPEAT_DELAY_MS);
    }

    private void cancelRepeat() {
        if (activeDelayHandle != -1) {
            Window.clearTimeout(activeDelayHandle);
            activeDelayHandle = -1;
        }
        if (activeRepeatHandle != -1) {
            Window.clearInterval(activeRepeatHandle);
            activeRepeatHandle = -1;
        }
    }

    private void dispatch(Action action) {
        if (action == Action.RESET) {
            restartHandler.run();
            return;
        }
        GameEngine engine = engineSupplier.get();
        if (engine == null) {
            return;
        }
        switch (action) {
            case MOVE_LEFT -> engine.moveLeft();
            case MOVE_RIGHT -> engine.moveRight();
            case SOFT_DROP -> engine.softDrop();
            case HARD_DROP -> { engine.hardDrop(); sound.play("harddrop"); }
            case ROTATE_CW -> { engine.rotateCw(); sound.play("rotate"); }
            case ROTATE_CCW -> { engine.rotateCcw(); sound.play("rotate"); }
            case HOLD -> { engine.hold(); sound.play("hold"); }
            case SELECT_DOWN -> engine.selectDirectionDown();
            case SELECT_UP -> engine.selectDirectionUp();
            case PAUSE -> engine.togglePause();
            case RESET -> { /* handled above */ }
        }
    }
}
