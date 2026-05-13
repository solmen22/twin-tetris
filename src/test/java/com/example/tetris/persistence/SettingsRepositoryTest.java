package com.example.tetris.persistence;

import com.example.tetris.game.GameMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SettingsRepositoryTest {

    @Test
    void 初回はデフォルトの_USER_CHOICE_を返す(@TempDir Path tmp) {
        SettingsRepository repo = new SettingsRepository(tmp.resolve("settings.json"));

        assertThat(repo.lastMode()).isEqualTo(GameMode.USER_CHOICE);
    }

    @Test
    void saveLastMode後にlastModeで参照できる(@TempDir Path tmp) {
        SettingsRepository repo = new SettingsRepository(tmp.resolve("settings.json"));

        repo.saveLastMode(GameMode.ALTERNATING);

        assertThat(repo.lastMode()).isEqualTo(GameMode.ALTERNATING);
    }

    @Test
    void 保存後に新規Repositoryから読み込める(@TempDir Path tmp) {
        Path path = tmp.resolve("settings.json");
        SettingsRepository writer = new SettingsRepository(path);
        writer.saveLastMode(GameMode.RANDOM);

        SettingsRepository reader = new SettingsRepository(path);

        assertThat(reader.lastMode()).isEqualTo(GameMode.RANDOM);
    }
}
