package com.example.tetris.persistence;

import com.example.tetris.game.GameMode;
import com.fasterxml.jackson.core.type.TypeReference;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class HighScoreRepository {

    public static final int TOP_N = 10;
    private static final String DEFAULT_DIR = ".bidirectional-tetris";
    private static final String DEFAULT_FILE = "highscores.json";

    private final JsonStore<HighScoreData> store;
    private HighScoreData cache;

    public HighScoreRepository() {
        this(defaultPath());
    }

    public HighScoreRepository(Path path) {
        this.store = new JsonStore<>(path, new TypeReference<HighScoreData>() {});
    }

    public static Path defaultPath() {
        return Paths.get(System.getProperty("user.home"), DEFAULT_DIR, DEFAULT_FILE);
    }

    public List<HighScoreEntry> top(GameMode mode) {
        return ensureLoaded().entriesFor(mode);
    }

    public boolean qualifies(GameMode mode, long score) {
        List<HighScoreEntry> existing = top(mode);
        if (existing.size() < TOP_N) {
            return score > 0;
        }
        return existing.get(TOP_N - 1).points() < score;
    }

    public void add(GameMode mode, HighScoreEntry entry) {
        HighScoreData updated = ensureLoaded().withEntryAdded(mode, entry, TOP_N);
        cache = updated;
        store.save(updated);
    }

    public Path filePath() {
        return store.filePath();
    }

    private HighScoreData ensureLoaded() {
        if (cache == null) {
            cache = store.load().orElseGet(HighScoreData::empty);
        }
        return cache;
    }
}
