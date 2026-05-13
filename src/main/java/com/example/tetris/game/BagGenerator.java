package com.example.tetris.game;

import com.example.tetris.domain.TetrominoType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public final class BagGenerator implements PieceProvider {

    private static final TetrominoType[] BAG_TEMPLATE = TetrominoType.values();

    private final Random random;
    private final Deque<TetrominoType> bag = new ArrayDeque<>();

    public BagGenerator() {
        this(new Random());
    }

    public BagGenerator(Random random) {
        this.random = random;
    }

    @Override
    public TetrominoType next() {
        if (bag.isEmpty()) {
            refill();
        }
        return bag.poll();
    }

    public int remainingInCurrentBag() {
        return bag.size();
    }

    private void refill() {
        List<TetrominoType> shuffled = new ArrayList<>(BAG_TEMPLATE.length);
        Collections.addAll(shuffled, BAG_TEMPLATE);
        Collections.shuffle(shuffled, random);
        bag.addAll(shuffled);
    }
}
