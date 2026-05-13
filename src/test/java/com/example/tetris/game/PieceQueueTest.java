package com.example.tetris.game;

import com.example.tetris.domain.TetrominoType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PieceQueueTest {

    @Test
    void デフォルトのpreviewSizeは3() {
        PieceQueue queue = new PieceQueue(new TestPieceProvider(TetrominoType.T));

        assertThat(queue.previewSize()).isEqualTo(3);
        assertThat(queue.peek()).hasSize(3);
    }

    @Test
    void peekは消費せず同じ並びを返す() {
        TestPieceProvider source = new TestPieceProvider(
            List.of(TetrominoType.I, TetrominoType.O, TetrominoType.T, TetrominoType.S),
            TetrominoType.Z
        );
        PieceQueue queue = new PieceQueue(source, 3);

        List<TetrominoType> first = queue.peek();
        List<TetrominoType> second = queue.peek();

        assertThat(first).containsExactly(TetrominoType.I, TetrominoType.O, TetrominoType.T);
        assertThat(second).isEqualTo(first);
    }

    @Test
    void nextは先頭を消費し新しいピースで補充する() {
        TestPieceProvider source = new TestPieceProvider(
            List.of(TetrominoType.I, TetrominoType.O, TetrominoType.T, TetrominoType.S),
            TetrominoType.Z
        );
        PieceQueue queue = new PieceQueue(source, 3);

        TetrominoType first = queue.next();

        assertThat(first).isEqualTo(TetrominoType.I);
        assertThat(queue.peek()).containsExactly(TetrominoType.O, TetrominoType.T, TetrominoType.S);
    }

    @Test
    void previewSize0以下は例外() {
        assertThatThrownBy(() -> new PieceQueue(new TestPieceProvider(TetrominoType.T), 0))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
