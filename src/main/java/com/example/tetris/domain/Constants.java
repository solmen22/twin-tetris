package com.example.tetris.domain;

public final class Constants {

    public static final int BOARD_WIDTH = 10;
    public static final int BOARD_HEIGHT = 21;

    public static final int CENTER_ROW = 10;
    public static final int UPPER_FIELD_TOP = 0;
    public static final int UPPER_FIELD_BOTTOM = 9;
    public static final int LOWER_FIELD_TOP = 11;
    public static final int LOWER_FIELD_BOTTOM = 20;

    public static final int SPAWN_DOWN_COL = 3;
    public static final int SPAWN_DOWN_ROW = 0;
    public static final int SPAWN_UP_COL = 3;
    public static final int SPAWN_UP_ROW = BOARD_HEIGHT - 4;

    private Constants() {
    }
}
