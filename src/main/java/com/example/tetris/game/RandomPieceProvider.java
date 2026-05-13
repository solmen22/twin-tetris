package com.example.tetris.game;

import com.example.tetris.domain.TetrominoType;

import java.util.Random;

public final class RandomPieceProvider implements PieceProvider {

    private static final TetrominoType[] TYPES = TetrominoType.values();

    private final Random random;

    public RandomPieceProvider() {
        this(new Random());
    }

    public RandomPieceProvider(Random random) {
        this.random = random;
    }

    @Override
    public TetrominoType next() {
        return TYPES[random.nextInt(TYPES.length)];
    }
}
