package com.example.tetris.game;

import com.example.tetris.domain.Direction;

import java.util.Random;

public final class RandomDirectionStrategy implements DirectionStrategy {

    private final Random random;

    public RandomDirectionStrategy() {
        this(new Random());
    }

    public RandomDirectionStrategy(Random random) {
        this.random = random;
    }

    @Override
    public Direction next() {
        return random.nextBoolean() ? Direction.DOWN : Direction.UP;
    }
}
