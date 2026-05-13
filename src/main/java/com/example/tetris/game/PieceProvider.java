package com.example.tetris.game;

import com.example.tetris.domain.TetrominoType;

@FunctionalInterface
public interface PieceProvider {
    TetrominoType next();
}
