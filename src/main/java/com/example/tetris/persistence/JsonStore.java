package com.example.tetris.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class JsonStore<T> {

    private static final Logger log = LoggerFactory.getLogger(JsonStore.class);

    private final ObjectMapper mapper;
    private final Path filePath;
    private final TypeReference<T> typeRef;

    public JsonStore(Path filePath, TypeReference<T> typeRef) {
        this.filePath = filePath;
        this.typeRef = typeRef;
        this.mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public Path filePath() {
        return filePath;
    }

    public Optional<T> load() {
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        try {
            return Optional.of(mapper.readValue(filePath.toFile(), typeRef));
        } catch (IOException e) {
            log.warn("Failed to read {}: {}", filePath, e.getMessage());
            return Optional.empty();
        }
    }

    public void save(T data) {
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            mapper.writeValue(filePath.toFile(), data);
        } catch (IOException e) {
            log.warn("Failed to write {}: {}", filePath, e.getMessage());
        }
    }
}
