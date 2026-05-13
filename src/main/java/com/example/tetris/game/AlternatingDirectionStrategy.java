package com.example.tetris.game;

import com.example.tetris.domain.Direction;

public final class AlternatingDirectionStrategy implements DirectionStrategy {

    private Direction nextDirection;

    public AlternatingDirectionStrategy() {
        this(Direction.DOWN);
    }

    public AlternatingDirectionStrategy(Direction firstDirection) {
        this.nextDirection = firstDirection;
    }

    @Override
    public Direction next() {
        Direction result = nextDirection;
        nextDirection = result.opposite();
        return result;
    }
}
