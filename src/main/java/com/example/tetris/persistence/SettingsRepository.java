package com.example.tetris.persistence;

import com.example.tetris.game.GameMode;
import com.fasterxml.jackson.core.type.TypeReference;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class SettingsRepository {

    private static final String DEFAULT_DIR = ".bidirectional-tetris";
    private static final String DEFAULT_FILE = "settings.json";

    private final JsonStore<SettingsData> store;
    private SettingsData cache;

    public SettingsRepository() {
        this(defaultPath());
    }

    public SettingsRepository(Path path) {
        this.store = new JsonStore<>(path, new TypeReference<SettingsData>() {});
    }

    public static Path defaultPath() {
        return Paths.get(System.getProperty("user.home"), DEFAULT_DIR, DEFAULT_FILE);
    }

    public GameMode lastMode() {
        SettingsData data = ensureLoaded();
        try {
            return GameMode.valueOf(data.lastMode());
        } catch (IllegalArgumentException ignored) {
            return GameMode.USER_CHOICE;
        }
    }

    public void saveLastMode(GameMode mode) {
        cache = new SettingsData(mode.name());
        store.save(cache);
    }

    public Path filePath() {
        return store.filePath();
    }

    private SettingsData ensureLoaded() {
        if (cache == null) {
            cache = store.load().orElseGet(SettingsData::defaults);
        }
        return cache;
    }
}
