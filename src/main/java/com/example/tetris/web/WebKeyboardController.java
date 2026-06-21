package com.example.tetris.web;

import com.example.tetris.domain.Tetromino;
import com.example.tetris.game.GameEngine;
import org.teavm.jso.dom.events.KeyboardEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * キーボード入力を処理する。SPEC 8.2 の DAS / ARR を実装するため、押下中のキーを
 * 状態として保持し、毎フレーム {@link #update(double)} で自動連射を駆動する。
 */
public final class WebKeyboardController {

    private final Supplier<GameEngine> engineSupplier;
    private final Runnable restartHandler;
    private final SettingsStore settings;
    private final SoundManager sound;

    private final Set<String> heldCodes = new HashSet<>();
    private boolean leftHeld;
    private boolean rightHeld;
    private boolean softHeld;
    private int shiftDir;          // -1 左 / 0 なし / +1 右
    private double dasTimer;
    private double arrTimer;
    private boolean dasCharged;
    private double softTimer;
    private boolean hardDropFlag;

    public WebKeyboardController(
        Supplier<GameEngine> engineSupplier,
        Runnable restartHandler,
        SettingsStore settings,
        SoundManager sound
    ) {
        this.engineSupplier = engineSupplier;
        this.restartHandler = restartHandler;
        this.settings = settings;
        this.sound = sound;
    }

    /** keydown 処理。アクションを処理したら true。 */
    public boolean onKeyDown(KeyboardEvent event) {
        String code = event.getCode();
        if (code == null || code.isEmpty()) {
            return false;
        }
        Action action = settings.actionForCode(code);
        if (action == null) {
            return false;
        }
        boolean isRepeat = heldCodes.contains(code);
        heldCodes.add(code);
        if (isRepeat) {
            // OS のオートリピートは無視。連射は DAS / ARR で自前制御する。
            return true;
        }
        switch (action) {
            case MOVE_LEFT -> { leftHeld = true; startShift(-1); }
            case MOVE_RIGHT -> { rightHeld = true; startShift(1); }
            case SOFT_DROP -> { softHeld = true; softTimer = 0.0; softStep(); }
            case HARD_DROP -> hardDrop();
            case ROTATE_CW -> rotate(true);
            case ROTATE_CCW -> rotate(false);
            case HOLD -> hold();
            case SELECT_DOWN -> select(true);
            case SELECT_UP -> select(false);
            case PAUSE -> togglePause();
            case RESET -> restartHandler.run();
        }
        return true;
    }

    public void onKeyUp(KeyboardEvent event) {
        String code = event.getCode();
        if (code == null || code.isEmpty()) {
            return;
        }
        heldCodes.remove(code);
        Action action = settings.actionForCode(code);
        if (action == null) {
            return;
        }
        switch (action) {
            case MOVE_LEFT -> { leftHeld = false; recomputeShiftDir(); }
            case MOVE_RIGHT -> { rightHeld = false; recomputeShiftDir(); }
            case SOFT_DROP -> softHeld = false;
            default -> { }
        }
    }

    /** 毎フレーム呼び出し、DAS / ARR とソフトドロップの自動連射を進める。 */
    public void update(double deltaMs) {
        GameEngine engine = engineSupplier.get();
        if (engine == null) {
            return;
        }
        if (engine.state().paused() || engine.state().gameOver()) {
            return;
        }
        if (shiftDir != 0) {
            dasTimer += deltaMs;
            if (!dasCharged) {
                if (dasTimer >= settings.das()) {
                    dasCharged = true;
                    arrTimer = 0.0;
                    shift(shiftDir, false);
                }
            } else {
                int arr = settings.arr();
                if (arr <= 0) {
                    while (shift(shiftDir, false)) {
                        // 0ms ARR は壁まで一気に移動
                    }
                } else {
                    arrTimer += deltaMs;
                    while (arrTimer >= arr) {
                        arrTimer -= arr;
                        if (!shift(shiftDir, false)) {
                            break;
                        }
                    }
                }
            }
        }
        if (softHeld) {
            int sdf = settings.softDropInterval();
            if (sdf <= 0) {
                while (softStepSilent()) {
                    // 0ms は接地まで一気に
                }
            } else {
                softTimer += deltaMs;
                while (softTimer >= sdf) {
                    softTimer -= sdf;
                    if (!softStepSilent()) {
                        break;
                    }
                }
            }
        }
    }

    /** ゲーム開始・終了・ポーズ・メニュー遷移時に押下状態をリセットする。 */
    public void releaseAll() {
        heldCodes.clear();
        leftHeld = false;
        rightHeld = false;
        softHeld = false;
        shiftDir = 0;
        dasTimer = 0.0;
        arrTimer = 0.0;
        dasCharged = false;
        softTimer = 0.0;
    }

    /** ハードドロップ直後のロック音を抑制するためのフラグ取得(取得で消費)。 */
    public boolean consumeHardDropFlag() {
        boolean flag = hardDropFlag;
        hardDropFlag = false;
        return flag;
    }

    private void startShift(int dir) {
        shiftDir = dir;
        dasTimer = 0.0;
        arrTimer = 0.0;
        dasCharged = false;
        shift(dir, true);
    }

    private void recomputeShiftDir() {
        int next;
        if (leftHeld && !rightHeld) {
            next = -1;
        } else if (rightHeld && !leftHeld) {
            next = 1;
        } else if (leftHeld && rightHeld) {
            next = shiftDir != 0 ? shiftDir : -1;
        } else {
            next = 0;
        }
        if (next != shiftDir) {
            shiftDir = next;
            dasTimer = 0.0;
            arrTimer = 0.0;
            dasCharged = false;
            if (shiftDir != 0) {
                shift(shiftDir, true);
            }
        }
    }

    private boolean shift(int dir, boolean withSound) {
        GameEngine engine = engineSupplier.get();
        if (engine == null) {
            return false;
        }
        Tetromino before = engine.state().currentPiece();
        if (dir < 0) {
            engine.moveLeft();
        } else {
            engine.moveRight();
        }
        Tetromino after = engine.state().currentPiece();
        boolean moved = before != null && after != null && !before.origin().equals(after.origin());
        if (moved && withSound) {
            sound.play("move");
        }
        return moved;
    }

    private boolean softStep() {
        return softStepSilent();
    }

    private boolean softStepSilent() {
        GameEngine engine = engineSupplier.get();
        if (engine == null) {
            return false;
        }
        Tetromino before = engine.state().currentPiece();
        engine.softDrop();
        Tetromino after = engine.state().currentPiece();
        return before != null && after != null && !before.origin().equals(after.origin());
    }

    private void hardDrop() {
        GameEngine engine = engineSupplier.get();
        if (engine == null || engine.state().currentPiece() == null
            || engine.state().paused() || engine.state().gameOver()) {
            return;
        }
        engine.hardDrop();
        hardDropFlag = true;
        sound.play("harddrop");
    }

    private void rotate(boolean cw) {
        GameEngine engine = engineSupplier.get();
        if (engine == null) {
            return;
        }
        Tetromino before = engine.state().currentPiece();
        if (cw) {
            engine.rotateCw();
        } else {
            engine.rotateCcw();
        }
        Tetromino after = engine.state().currentPiece();
        if (before != null && after != null
            && (before.rotation() != after.rotation() || !before.origin().equals(after.origin()))) {
            sound.play("rotate");
        }
    }

    private void hold() {
        GameEngine engine = engineSupplier.get();
        if (engine == null) {
            return;
        }
        Tetromino before = engine.state().currentPiece();
        engine.hold();
        Tetromino after = engine.state().currentPiece();
        if (before != null && after != null && before.type() != after.type()) {
            sound.play("hold");
        }
    }

    private void select(boolean down) {
        GameEngine engine = engineSupplier.get();
        if (engine == null) {
            return;
        }
        if (down) {
            engine.selectDirectionDown();
        } else {
            engine.selectDirectionUp();
        }
    }

    private void togglePause() {
        GameEngine engine = engineSupplier.get();
        if (engine != null) {
            engine.togglePause();
        }
    }
}
