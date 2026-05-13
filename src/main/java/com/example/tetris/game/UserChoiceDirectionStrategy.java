package com.example.tetris.game;

import com.example.tetris.domain.Direction;

public final class UserChoiceDirectionStrategy implements DirectionStrategy {

    private Direction pending;

    public UserChoiceDirectionStrategy() {
        this(Direction.DOWN);
    }

    public UserChoiceDirectionStrategy(Direction initial) {
        this.pending = initial;
    }

    public Direction pending() {
        return pending;
    }

    public void selectDown() {
        this.pending = Direction.DOWN;
    }

    public void selectUp() {
        this.pending = Direction.UP;
    }

    @Override
    public Direction next() {
        return pending;
    }
}
