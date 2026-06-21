package com.example.tetris.game;

import com.example.tetris.domain.Board;
import com.example.tetris.domain.Direction;
import com.example.tetris.domain.Position;
import com.example.tetris.domain.Score;
import com.example.tetris.domain.Tetromino;
import com.example.tetris.domain.TetrominoType;

public final class GameEngine {

    private static final double GRAVITY_LEVEL_1_MS = 1000.0;
    private static final double GRAVITY_BASE = 0.82;
    private static final double GRAVITY_MIN_MS = 16.0;
    private static final double USER_CHOICE_GRACE_MS = 500.0;
    private static final double CLEAR_FLASH_DURATION_MS = 300.0;
    // SPEC 8.2: 接地後 0.5 秒のロックディレイ。移動・回転で再チャージするが
    // 無限ループ防止のため再チャージ回数に上限を設ける。
    private static final double LOCK_DELAY_MS = 500.0;
    private static final int LOCK_RESET_MAX = 15;

    private final Board board;
    private final PieceQueue pieceQueue;
    private final DirectionStrategy directionStrategy;
    private final GameMode mode;
    private final HoldSlot holdSlot;

    private Tetromino current;
    private Score score;
    private boolean gameOver;
    private boolean paused;
    private double gravityAccumulatedMs;
    private boolean inSpawnGrace;
    private double spawnGraceRemainingMs;
    private double clearFlashRemainingMs;
    private int clearFlashMultiplier;
    private boolean locking;
    private double lockTimerMs;
    private int lockResets;
    private boolean backToBackActive;
    // 現在ミノが属する半面(true=下半面)。中央境界の判定に使う。
    // 通常は向き(UP=下半面)と一致するが、方向切替後も「いた半面」を保持する。
    private boolean currentLower;

    // ランタイム統計(GameStats に集約してスナップショット化する)
    private int piecesPlaced;
    private int centerBoundaryClears;
    private int maxChain;
    private int simultaneousClears;
    private long elapsedMs;

    public GameEngine(PieceProvider pieceProvider) {
        this(pieceProvider, DirectionStrategy.alwaysDown(), GameMode.RANDOM);
    }

    public GameEngine(PieceProvider pieceProvider, DirectionStrategy directionStrategy) {
        this(pieceProvider, directionStrategy, GameMode.RANDOM);
    }

    public GameEngine(PieceProvider pieceProvider, DirectionStrategy directionStrategy, GameMode mode) {
        this.board = new Board();
        this.pieceQueue = pieceProvider instanceof PieceQueue pq ? pq : new PieceQueue(pieceProvider);
        this.directionStrategy = directionStrategy;
        this.mode = mode;
        this.holdSlot = new HoldSlot();
        this.score = Score.initial();
        this.gameOver = false;
        this.paused = false;
        this.gravityAccumulatedMs = 0.0;
        this.inSpawnGrace = false;
        this.spawnGraceRemainingMs = 0.0;
        this.locking = false;
        this.lockTimerMs = 0.0;
        this.lockResets = 0;
        this.backToBackActive = false;
        this.currentLower = false;
        this.piecesPlaced = 0;
        this.centerBoundaryClears = 0;
        this.maxChain = 0;
        this.simultaneousClears = 0;
        this.elapsedMs = 0L;
        spawnFromQueue();
    }

    public void tick(double deltaMs) {
        if (gameOver || paused || current == null) {
            return;
        }
        elapsedMs += (long) deltaMs;
        if (inSpawnGrace) {
            spawnGraceRemainingMs -= deltaMs;
            if (spawnGraceRemainingMs <= 0) {
                inSpawnGrace = false;
                spawnGraceRemainingMs = 0.0;
            }
        }
        if (clearFlashRemainingMs > 0) {
            clearFlashRemainingMs -= deltaMs;
            if (clearFlashRemainingMs < 0) {
                clearFlashRemainingMs = 0;
            }
        }
        gravityAccumulatedMs += deltaMs;
        double period = gravityPeriodMs(score.level());
        while (gravityAccumulatedMs >= period && !gameOver && current != null) {
            gravityAccumulatedMs -= period;
            Tetromino moved = current.translated(current.direction().rowStep(), 0);
            if (CollisionDetector.collides(board, moved, currentLower)) {
                // これ以上落下できない。ロックディレイに委ねるため重力消費を止める。
                gravityAccumulatedMs = 0.0;
                break;
            }
            current = moved;
            locking = false;
        }

        applyLockDelay(deltaMs);
    }

    /** 接地している間ロックタイマーを進め、満了でロックする(SPEC 8.2)。 */
    private void applyLockDelay(double deltaMs) {
        if (gameOver || current == null) {
            return;
        }
        if (isResting()) {
            if (!locking) {
                locking = true;
                lockTimerMs = LOCK_DELAY_MS;
            } else {
                lockTimerMs -= deltaMs;
                if (lockTimerMs <= 0.0) {
                    lockAndRespawn();
                }
            }
        } else {
            locking = false;
        }
    }

    /** 現在ミノがこれ以上中央方向へ進めない(接地している)か。 */
    private boolean isResting() {
        if (current == null) {
            return false;
        }
        Tetromino moved = current.translated(current.direction().rowStep(), 0);
        return CollisionDetector.collides(board, moved, currentLower);
    }

    /** 接地中の移動・回転成功でロックタイマーを再チャージする(上限あり)。 */
    private void onPieceManipulated() {
        if (locking && lockResets < LOCK_RESET_MAX) {
            lockResets++;
            lockTimerMs = LOCK_DELAY_MS;
        }
    }

    public void moveLeft() {
        tryHorizontalMove(-1);
    }

    public void moveRight() {
        tryHorizontalMove(1);
    }

    private void tryHorizontalMove(int dcol) {
        if (!canActOnPiece()) {
            return;
        }
        Tetromino moved = current.translated(0, dcol);
        if (!CollisionDetector.collides(board, moved, currentLower)) {
            current = moved;
            onPieceManipulated();
        }
    }

    public void softDrop() {
        if (!canActOnPiece()) {
            return;
        }
        int step = current.direction().rowStep();
        Tetromino moved = current.translated(step, 0);
        if (!CollisionDetector.collides(board, moved, currentLower)) {
            current = moved;
            score = score.addPoints(ScoringService.softDropPoints(1));
            gravityAccumulatedMs = 0.0;
        }
    }

    public void hardDrop() {
        if (!canActOnPiece()) {
            return;
        }
        int step = current.direction().rowStep();
        int cells = 0;
        while (true) {
            Tetromino moved = current.translated(step, 0);
            if (CollisionDetector.collides(board, moved, currentLower)) {
                break;
            }
            current = moved;
            cells++;
        }
        score = score.addPoints(ScoringService.hardDropPoints(cells));
        lockAndRespawn();
    }

    public void rotateCw() {
        if (!canActOnPiece()) {
            return;
        }
        RotationSystem.tryRotateCw(board, current, currentLower).ifPresent(p -> {
            current = p;
            onPieceManipulated();
        });
    }

    public void rotateCcw() {
        if (!canActOnPiece()) {
            return;
        }
        RotationSystem.tryRotateCcw(board, current, currentLower).ifPresent(p -> {
            current = p;
            onPieceManipulated();
        });
    }

    public void hold() {
        if (!canActOnPiece() || !holdSlot.canHold()) {
            return;
        }
        TetrominoType currentType = current.type();
        TetrominoType previousHeld = holdSlot.swap(currentType);
        if (previousHeld != null) {
            spawnPiece(previousHeld, directionStrategy.next());
        } else {
            TetrominoType nextType = pieceQueue.next();
            spawnPiece(nextType, directionStrategy.next());
        }
        if (mode == GameMode.USER_CHOICE && !gameOver && current != null) {
            startSpawnGrace();
        }
    }

    public void selectDirectionDown() {
        applyUserDirection(Direction.DOWN);
    }

    public void selectDirectionUp() {
        applyUserDirection(Direction.UP);
    }

    private void applyUserDirection(Direction direction) {
        if (gameOver || mode != GameMode.USER_CHOICE) {
            return;
        }
        if (directionStrategy instanceof UserChoiceDirectionStrategy ucs) {
            if (direction == Direction.DOWN) {
                ucs.selectDown();
            } else {
                ucs.selectUp();
            }
        }
        // 落下位置(行・列・回転)はそのまま、重力の向きだけを反転する。
        // ミノは「いま居る半面」に留まり、中央線は越えない。例えば下半面で中央まで
        // 上昇したミノを DOWN にすると、その場(中央)から今度は下へ落下し始める。
        if (current != null && current.direction() != direction) {
            Tetromino flipped = new Tetromino(
                current.type(), current.origin(), current.rotation(), direction);
            if (!CollisionDetector.collides(board, flipped, currentLower)) {
                current = flipped;
                gravityAccumulatedMs = 0.0;
                locking = false;
                lockTimerMs = 0.0;
                lockResets = 0;
            }
        }
    }

    public void togglePause() {
        if (!gameOver) {
            paused = !paused;
        }
    }

    private boolean canActOnPiece() {
        return !gameOver && !paused && current != null;
    }

    private void lockAndRespawn() {
        TetrominoType type = current.type();
        for (Position p : current.cells()) {
            board.place(p, type);
        }
        piecesPlaced++;
        locking = false;
        LineClearService.LineClearResult result = LineClearService.processClears(board);
        if (!result.isEmpty()) {
            boolean tetris = ScoringService.isTetrisResult(result);
            boolean backToBack = tetris && backToBackActive;
            int total = result.totalLines();
            score = score.addLines(total);
            int newLevel = score.level();
            score = score.addPoints(ScoringService.resultPoints(result, newLevel, backToBack));
            backToBackActive = tetris;
            triggerClearFlash(result);
        }
        updateStatsForResult(result);
        holdSlot.unlock();
        spawnFromQueue();
    }

    private void updateStatsForResult(LineClearService.LineClearResult result) {
        if (result.isEmpty()) {
            return;
        }
        for (LineClearService.CascadeStep step : result.steps()) {
            if (step.centerCleared()) {
                centerBoundaryClears++;
            }
            if (step.isSimultaneous()) {
                simultaneousClears++;
            }
        }
        if (result.chainCount() > maxChain) {
            maxChain = result.chainCount();
        }
    }

    private void spawnFromQueue() {
        TetrominoType type = pieceQueue.next();
        Direction direction = directionStrategy.next();
        spawnPiece(type, direction);
        if (mode == GameMode.USER_CHOICE && !gameOver && current != null) {
            startSpawnGrace();
        }
    }

    private void spawnPiece(TetrominoType type, Direction direction) {
        Tetromino piece = direction == Direction.DOWN
            ? Tetromino.spawnDown(type)
            : Tetromino.spawnUp(type);
        // 出現側で半面が決まる: DOWN=上端から=上半面、UP=下端から=下半面。
        boolean lower = direction == Direction.UP;
        if (CollisionDetector.collides(board, piece, lower)) {
            gameOver = true;
            current = null;
            return;
        }
        current = piece;
        currentLower = lower;
        gravityAccumulatedMs = 0.0;
        locking = false;
        lockTimerMs = 0.0;
        lockResets = 0;
    }

    private void startSpawnGrace() {
        inSpawnGrace = true;
        spawnGraceRemainingMs = USER_CHOICE_GRACE_MS;
    }

    private void triggerClearFlash(LineClearService.LineClearResult result) {
        int maxMultiplier = 1;
        for (LineClearService.CascadeStep step : result.steps()) {
            int m = ScoringService.multiplierFor(step);
            if (m > maxMultiplier) {
                maxMultiplier = m;
            }
        }
        clearFlashMultiplier = maxMultiplier;
        clearFlashRemainingMs = CLEAR_FLASH_DURATION_MS;
    }

    private static double gravityPeriodMs(int level) {
        double period = GRAVITY_LEVEL_1_MS * Math.pow(GRAVITY_BASE, level - 1);
        return Math.max(GRAVITY_MIN_MS, period);
    }

    public GameState state() {
        Direction pendingDirection = null;
        if (mode == GameMode.USER_CHOICE && directionStrategy instanceof UserChoiceDirectionStrategy ucs) {
            pendingDirection = ucs.pending();
        }
        double progress = clearFlashRemainingMs > 0
            ? clearFlashRemainingMs / CLEAR_FLASH_DURATION_MS
            : 0.0;
        GameStats stats = new GameStats(
            piecesPlaced,
            centerBoundaryClears,
            maxChain,
            simultaneousClears,
            elapsedMs
        );
        return new GameState(
            board,
            current,
            score,
            stats,
            gameOver,
            paused,
            mode,
            holdSlot.type(),
            pieceQueue.peek(),
            pendingDirection,
            inSpawnGrace,
            progress,
            clearFlashRemainingMs > 0 ? clearFlashMultiplier : 0
        );
    }
}
