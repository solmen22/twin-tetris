package com.example.tetris.game;

import com.example.tetris.domain.Direction;

@FunctionalInterface
public interface DirectionStrategy {

    Direction next();

    static DirectionStrategy alwaysDown() {
        return () -> Direction.DOWN;
    }

    static DirectionStrategy alternating() {
        return new AlternatingDirectionStrategy();
    }
}
