package com.example.tetris.persistence;

import com.example.tetris.game.GameMode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record HighScoreData(
    List<HighScoreEntry> random,
    List<HighScoreEntry> alternating,
    List<HighScoreEntry> userChoice
) {

    @JsonCreator
    public HighScoreData(
        @JsonProperty("random") List<HighScoreEntry> random,
        @JsonProperty("alternating") List<HighScoreEntry> alternating,
        @JsonProperty("userChoice") List<HighScoreEntry> userChoice
    ) {
        this.random = random == null ? List.of() : List.copyOf(random);
        this.alternating = alternating == null ? List.of() : List.copyOf(alternating);
        this.userChoice = userChoice == null ? List.of() : List.copyOf(userChoice);
    }

    public static HighScoreData empty() {
        return new HighScoreData(List.of(), List.of(), List.of());
    }

    public List<HighScoreEntry> entriesFor(GameMode mode) {
        return switch (mode) {
            case RANDOM -> random;
            case ALTERNATING -> alternating;
            case USER_CHOICE -> userChoice;
        };
    }

    public HighScoreData withEntryAdded(GameMode mode, HighScoreEntry entry, int topN) {
        List<HighScoreEntry> updated = new ArrayList<>(entriesFor(mode));
        updated.add(entry);
        updated.sort(Comparator.comparingLong(HighScoreEntry::points).reversed());
        if (updated.size() > topN) {
            updated = new ArrayList<>(updated.subList(0, topN));
        }
        return switch (mode) {
            case RANDOM -> new HighScoreData(updated, alternating, userChoice);
            case ALTERNATING -> new HighScoreData(random, updated, userChoice);
            case USER_CHOICE -> new HighScoreData(random, alternating, updated);
        };
    }
}
