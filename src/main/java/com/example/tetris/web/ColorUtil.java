package com.example.tetris.web;

import com.example.tetris.domain.TetrominoType;

public final class ColorUtil {

    private ColorUtil() {
    }

    public static String rgb(TetrominoType type) {
        return "rgb(" + type.red() + ", " + type.green() + ", " + type.blue() + ")";
    }

    public static String rgbBrighter(TetrominoType type) {
        int r = Math.min(255, type.red() + 40);
        int g = Math.min(255, type.green() + 40);
        int b = Math.min(255, type.blue() + 40);
        return "rgb(" + r + ", " + g + ", " + b + ")";
    }
}
