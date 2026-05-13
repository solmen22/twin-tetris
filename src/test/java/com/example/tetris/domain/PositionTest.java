package com.example.tetris.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PositionTest {

    @Test
    void translateは元の位置を変えず新しい位置を返す() {
        Position p = new Position(5, 3);

        Position moved = p.translate(2, -1);

        assertThat(moved).isEqualTo(new Position(7, 2));
        assertThat(p).isEqualTo(new Position(5, 3));
    }

    @Test
    void 等価なPositionはequalsとhashCodeで等しい() {
        Position a = new Position(1, 2);
        Position b = new Position(1, 2);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void withRowは行のみを更新する() {
        assertThat(new Position(5, 3).withRow(0)).isEqualTo(new Position(0, 3));
    }

    @Test
    void withColは列のみを更新する() {
        assertThat(new Position(5, 3).withCol(9)).isEqualTo(new Position(5, 9));
    }
}
