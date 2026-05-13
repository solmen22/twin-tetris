package com.example.tetris.game;

import com.example.tetris.domain.Board;
import com.example.tetris.domain.Direction;
import com.example.tetris.domain.Score;
import com.example.tetris.domain.Tetromino;
import com.example.tetris.domain.TetrominoType;

import java.util.List;

public record GameState(
    Board board,
    Tetromino currentPiece,
    Score score,
    boolean gameOver,
    boolean paused,
    GameMode mode,
    TetrominoType heldType,
    List<TetrominoType> nextQueue,
    Direction pendingDirection,
    boolean inSpawnGrace,
    double clearFlashProgress,
    int clearFlashMultiplier
) {

    public GameState {
        nextQueue = nextQueue == null ? List.of() : List.copyOf(nextQueue);
    }
}
