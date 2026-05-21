package com.example.tetris.web;

public enum Action {
    MOVE_LEFT("左移動"),
    MOVE_RIGHT("右移動"),
    SOFT_DROP("ソフトドロップ"),
    HARD_DROP("ハードドロップ"),
    ROTATE_CW("右回転"),
    ROTATE_CCW("左回転"),
    HOLD("ホールド"),
    SELECT_DOWN("方向選択(下)"),
    SELECT_UP("方向選択(上)"),
    PAUSE("ポーズ"),
    RESET("リスタート");

    private final String label;

    Action(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
