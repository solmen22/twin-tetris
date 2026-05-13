package com.example.tetris.input;

import javafx.scene.input.KeyCode;

import java.util.EnumSet;
import java.util.Set;

public final class KeyBindings {

    public static final Set<KeyCode> MOVE_LEFT = EnumSet.of(KeyCode.LEFT);
    public static final Set<KeyCode> MOVE_RIGHT = EnumSet.of(KeyCode.RIGHT);
    public static final Set<KeyCode> SOFT_DROP = EnumSet.of(KeyCode.DOWN);
    public static final Set<KeyCode> HARD_DROP = EnumSet.of(KeyCode.SPACE);
    public static final Set<KeyCode> ROTATE_CW = EnumSet.of(KeyCode.UP, KeyCode.X);
    public static final Set<KeyCode> ROTATE_CCW = EnumSet.of(KeyCode.Z);
    public static final Set<KeyCode> PAUSE = EnumSet.of(KeyCode.ESCAPE, KeyCode.P);
    public static final Set<KeyCode> RESTART = EnumSet.of(KeyCode.R);

    private KeyBindings() {
    }
}
