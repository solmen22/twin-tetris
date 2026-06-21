package com.example.tetris.web;

import com.example.tetris.domain.TetrominoType;
import com.example.tetris.game.PieceProvider;

/**
 * 指定したミノ列を無限に繰り返す PieceProvider。
 * チュートリアルで決まった形のミノを供給するために使う。
 */
public final class LoopingPieceProvider implements PieceProvider {

    private final TetrominoType[] sequence;
    private int index;

    public LoopingPieceProvider(TetrominoType... sequence) {
        this.sequence = (sequence == null || sequence.length == 0)
            ? new TetrominoType[] {TetrominoType.O}
            : sequence;
    }

    @Override
    public TetrominoType next() {
        TetrominoType type = sequence[index % sequence.length];
        index++;
        return type;
    }
}
