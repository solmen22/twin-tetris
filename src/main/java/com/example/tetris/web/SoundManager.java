package com.example.tetris.web;

import org.teavm.jso.JSBody;

/**
 * 効果音マネージャ。実際の音声合成は {@code src/web/sfx.js}(Web Audio API)が担当し、
 * ここからは名前付きイベントを呼び出すだけのブリッジに徹する。
 * 設定(有効/無効・音量)は {@link SettingsStore} を参照する。
 */
public final class SoundManager {

    private final SettingsStore settings;

    public SoundManager(SettingsStore settings) {
        this.settings = settings;
        refresh();
    }

    /** 設定値(有効・音量)を JS 側へ反映する。 */
    public void refresh() {
        setEnabledJs(settings.soundEnabled());
        setVolumeJs(settings.soundVolume() / 100.0);
    }

    /** 自動再生ポリシー対策: ユーザー操作後に AudioContext を起こす。 */
    public void unlock() {
        unlockJs();
    }

    public void play(String name) {
        play(name, 0);
    }

    public void play(String name, int arg) {
        if (settings.soundEnabled()) {
            playJs(name, arg);
        }
    }

    @JSBody(params = {"name", "arg"}, script = "if (window.TetrisSfx) { window.TetrisSfx.play(name, arg); }")
    private static native void playJs(String name, int arg);

    @JSBody(script = "if (window.TetrisSfx) { window.TetrisSfx.unlock(); }")
    private static native void unlockJs();

    @JSBody(params = {"on"}, script = "if (window.TetrisSfx) { window.TetrisSfx.setEnabled(on); }")
    private static native void setEnabledJs(boolean on);

    @JSBody(params = {"v"}, script = "if (window.TetrisSfx) { window.TetrisSfx.setVolume(v); }")
    private static native void setVolumeJs(double v);
}
