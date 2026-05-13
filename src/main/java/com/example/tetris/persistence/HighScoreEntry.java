package com.example.tetris.persistence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record HighScoreEntry(
    long points,
    int level,
    int lines,
    String date
) {

    @JsonCreator
    public HighScoreEntry(
        @JsonProperty("points") long points,
        @JsonProperty("level") int level,
        @JsonProperty("lines") int lines,
        @JsonProperty("date") String date
    ) {
        this.points = points;
        this.level = level;
        this.lines = lines;
        this.date = date;
    }
}
