package com.example.tetris.persistence;

import com.example.tetris.game.GameMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HighScoreRepositoryTest {

    @Test
    void 初回はTop10が空(@TempDir Path tmp) {
        HighScoreRepository repo = new HighScoreRepository(tmp.resolve("highscores.json"));

        assertThat(repo.top(GameMode.RANDOM)).isEmpty();
        assertThat(repo.top(GameMode.ALTERNATING)).isEmpty();
        assertThat(repo.top(GameMode.USER_CHOICE)).isEmpty();
    }

    @Test
    void add後にtopで参照できる(@TempDir Path tmp) {
        HighScoreRepository repo = new HighScoreRepository(tmp.resolve("highscores.json"));

        repo.add(GameMode.RANDOM, new HighScoreEntry(1000, 3, 12, "2026-05-14"));

        assertThat(repo.top(GameMode.RANDOM)).hasSize(1);
        assertThat(repo.top(GameMode.RANDOM).get(0).points()).isEqualTo(1000);
    }

    @Test
    void エントリは降順にソートされる(@TempDir Path tmp) {
        HighScoreRepository repo = new HighScoreRepository(tmp.resolve("highscores.json"));

        repo.add(GameMode.RANDOM, new HighScoreEntry(500, 2, 8, "d"));
        repo.add(GameMode.RANDOM, new HighScoreEntry(2000, 5, 20, "d"));
        repo.add(GameMode.RANDOM, new HighScoreEntry(1000, 3, 12, "d"));

        List<HighScoreEntry> top = repo.top(GameMode.RANDOM);
        assertThat(top.get(0).points()).isEqualTo(2000);
        assertThat(top.get(1).points()).isEqualTo(1000);
        assertThat(top.get(2).points()).isEqualTo(500);
    }

    @Test
    void Top10を超えるエントリは下から切り捨てられる(@TempDir Path tmp) {
        HighScoreRepository repo = new HighScoreRepository(tmp.resolve("highscores.json"));
        for (int i = 1; i <= 15; i++) {
            repo.add(GameMode.RANDOM, new HighScoreEntry(i * 100L, 1, 0, "d"));
        }

        List<HighScoreEntry> top = repo.top(GameMode.RANDOM);

        assertThat(top).hasSize(10);
        assertThat(top.get(0).points()).isEqualTo(1500);
        assertThat(top.get(9).points()).isEqualTo(600);
    }

    @Test
    void qualifiesは満杯でないか最下位を上回るならtrue(@TempDir Path tmp) {
        HighScoreRepository repo = new HighScoreRepository(tmp.resolve("highscores.json"));
        for (int i = 1; i <= 10; i++) {
            repo.add(GameMode.RANDOM, new HighScoreEntry(i * 100L, 1, 0, "d"));
        }

        assertThat(repo.qualifies(GameMode.RANDOM, 150)).isTrue();
        assertThat(repo.qualifies(GameMode.RANDOM, 100)).isFalse();
        assertThat(repo.qualifies(GameMode.RANDOM, 0)).isFalse();
    }

    @Test
    void モードごとに独立して保存される(@TempDir Path tmp) {
        HighScoreRepository repo = new HighScoreRepository(tmp.resolve("highscores.json"));

        repo.add(GameMode.RANDOM, new HighScoreEntry(1000, 1, 0, "d"));
        repo.add(GameMode.ALTERNATING, new HighScoreEntry(2000, 1, 0, "d"));

        assertThat(repo.top(GameMode.RANDOM)).hasSize(1);
        assertThat(repo.top(GameMode.ALTERNATING)).hasSize(1);
        assertThat(repo.top(GameMode.USER_CHOICE)).isEmpty();
    }

    @Test
    void 保存後に新規Repository_で読み込める(@TempDir Path tmp) {
        Path path = tmp.resolve("highscores.json");
        HighScoreRepository writer = new HighScoreRepository(path);
        writer.add(GameMode.USER_CHOICE, new HighScoreEntry(9999, 8, 50, "2026-05-14T10:00:00"));

        HighScoreRepository reader = new HighScoreRepository(path);

        assertThat(reader.top(GameMode.USER_CHOICE)).hasSize(1);
        assertThat(reader.top(GameMode.USER_CHOICE).get(0).points()).isEqualTo(9999);
        assertThat(reader.top(GameMode.USER_CHOICE).get(0).level()).isEqualTo(8);
        assertThat(reader.top(GameMode.USER_CHOICE).get(0).lines()).isEqualTo(50);
    }
}
