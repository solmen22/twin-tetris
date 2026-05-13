package com.example.tetris.game;

import com.example.tetris.domain.TetrominoType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public final class TestPieceProvider implements PieceProvider {

    private final Deque<TetrominoType> queue;
    private final TetrominoType fallback;

    public TestPieceProvider(List<TetrominoType> types, TetrominoType fallback) {
        this.queue = new ArrayDeque<>(types);
        this.fallback = fallback;
    }

    public TestPieceProvider(TetrominoType single) {
        this(List.of(), single);
    }

    @Override
    public TetrominoType next() {
        if (queue.isEmpty()) {
            return fallback;
        }
        return queue.poll();
    }
}
