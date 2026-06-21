package com.example.tetris.web;

public final class WebConstants {

    public static final int CELL_SIZE = 30;
    public static final int MINI_CELL_SIZE = 22;

    public static final String BACKGROUND_COLOR = "#101020";
    public static final String GRID_LINE_COLOR = "#1f2030";
    public static final String EMPTY_CELL_COLOR = "#181828";
    public static final String CENTER_BOUNDARY_BG = "#2a1d3a";
    public static final String CENTER_BOUNDARY_LINE = "#ff66cc";
    public static final String HUD_TEXT_COLOR = "#e8e8f0";
    public static final String GAME_OVER_COLOR = "#ff5566";

    public static final double GRID_LINE_WIDTH = 1.0;
    public static final double CENTER_LINE_WIDTH = 2.5;

    public static final String HUD_FONT = "Consolas, monospace";
    public static final double TICK_MAX_MS = 100.0;

    // SPEC 10.4: 落下中ミノは半透明、確定ブロックは不透明。
    public static final double FALLING_PIECE_ALPHA = 0.82;
    // ゴースト(着地予測)の透明度。
    public static final double GHOST_ALPHA = 0.22;

    // SPEC 8.2: DAS / ARR の既定値(設定で変更可能)。
    public static final int DEFAULT_DAS_MS = 167;
    public static final int DEFAULT_ARR_MS = 33;
    // ソフトドロップの自動リピート間隔(SDF 相当)。
    public static final int DEFAULT_SOFT_DROP_MS = 25;

    private WebConstants() {
    }
}
