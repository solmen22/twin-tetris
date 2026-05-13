package com.example.tetris.game;

import com.example.tetris.domain.Board;
import com.example.tetris.domain.Score;
import com.example.tetris.domain.Tetromino;

public record GameState(
    Board board,
    Tetromino currentPiece,
    Score score,
    boolean gameOver,
    boolean paused
) {
}
