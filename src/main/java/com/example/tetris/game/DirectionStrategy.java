package com.example.tetris.game;

import com.example.tetris.domain.Direction;

import java.util.Random;

@FunctionalInterface
public interface DirectionStrategy {

    Direction next();

    static DirectionStrategy alwaysDown() {
        return () -> Direction.DOWN;
    }

    static DirectionStrategy alternating() {
        return new AlternatingDirectionStrategy();
    }

    static DirectionStrategy random() {
        return new RandomDirectionStrategy();
    }

    static DirectionStrategy random(Random random) {
        return new RandomDirectionStrategy(random);
    }

    static UserChoiceDirectionStrategy userChoice() {
        return new UserChoiceDirectionStrategy();
    }
}
