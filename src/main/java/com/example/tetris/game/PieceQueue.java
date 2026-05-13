package com.example.tetris.game;

import com.example.tetris.domain.TetrominoType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class PieceQueue implements PieceProvider {

    public static final int DEFAULT_PREVIEW_SIZE = 3;

    private final PieceProvider source;
    private final int previewSize;
    private final Deque<TetrominoType> preview = new ArrayDeque<>();

    public PieceQueue(PieceProvider source) {
        this(source, DEFAULT_PREVIEW_SIZE);
    }

    public PieceQueue(PieceProvider source, int previewSize) {
        if (previewSize < 1) {
            throw new IllegalArgumentException("previewSize must be at least 1");
        }
        this.source = source;
        this.previewSize = previewSize;
        refill();
    }

    @Override
    public TetrominoType next() {
        if (preview.isEmpty()) {
            refill();
        }
        TetrominoType type = preview.poll();
        refill();
        return type;
    }

    public List<TetrominoType> peek() {
        return new ArrayList<>(preview);
    }

    public int previewSize() {
        return previewSize;
    }

    private void refill() {
        while (preview.size() < previewSize) {
            preview.add(source.next());
        }
    }
}
