package com.example.tetris.ui;

import javafx.scene.paint.Color;

public final class UIConstants {

    public static final double CELL_SIZE = 30.0;
    public static final double BOARD_PADDING = 10.0;

    public static final double SCENE_WIDTH = 800.0;
    public static final double SCENE_HEIGHT = 900.0;

    public static final Color BACKGROUND_COLOR = Color.web("#101020");
    public static final Color GRID_LINE_COLOR = Color.web("#1f2030");
    public static final Color EMPTY_CELL_COLOR = Color.web("#181828");
    public static final Color CENTER_BOUNDARY_BG = Color.web("#2a1d3a");
    public static final Color CENTER_BOUNDARY_LINE = Color.web("#ff66cc");
    public static final Color HUD_TEXT_COLOR = Color.web("#e8e8f0");
    public static final Color GAME_OVER_COLOR = Color.web("#ff5566");

    public static final double GHOST_OPACITY = 0.25;
    public static final double GRID_LINE_WIDTH = 1.0;
    public static final double CENTER_LINE_WIDTH = 2.5;

    public static final String HUD_FONT_FAMILY = "Consolas";
    public static final double HUD_FONT_SIZE_LARGE = 22.0;
    public static final double HUD_FONT_SIZE_GAME_OVER = 48.0;

    public static final double GAMELOOP_TARGET_DT_MS = 1000.0 / 60.0;

    private UIConstants() {
    }
}
