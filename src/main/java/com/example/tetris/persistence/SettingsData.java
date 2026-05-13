package com.example.tetris.persistence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record SettingsData(
    String lastMode
) {

    @JsonCreator
    public SettingsData(@JsonProperty("lastMode") String lastMode) {
        this.lastMode = lastMode;
    }

    public static SettingsData defaults() {
        return new SettingsData("USER_CHOICE");
    }
}
