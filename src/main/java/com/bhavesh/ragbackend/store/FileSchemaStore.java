package com.bhavesh.ragbackend.store;

import com.bhavesh.ragbackend.dto.FieldDefinition;
import com.bhavesh.ragbackend.exception.SchemaException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FileSchemaStore implements SchemaStore {

    private static final Path ROOT_PATH = Paths.get("data/schemas");
    private static final Logger log = LoggerFactory.getLogger(FileSchemaStore.class);

    private final ObjectMapper objectMapper;

    @Override
    public void save(String folderId, String indexId, Map<String, FieldDefinition> schema) {
        try {

            Path folder = resolveFolder(folderId);

            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(resolveSchemaFile(folderId, indexId).toFile(), schema);

        }
        catch (Exception e) {
            log.error("Error saving schema for folderId={}, indexId={}", folderId, indexId, e);
            throw new SchemaException("Something went wrong while saving schema");
        }
    }

    @Override
    public Map<String, FieldDefinition> load(String folderId, String indexId) {
        Path schemaFile = resolveSchemaFile(folderId, indexId);

        if (!Files.exists(schemaFile)) {
            return null;
        }

        try {
            return objectMapper.readValue(
                    schemaFile.toFile(),
                    objectMapper.getTypeFactory()
                            .constructMapType(Map.class, String.class, FieldDefinition.class)
            );
        } catch (Exception e) {
            log.error("Error loading schema for folderId={}, indexId={}", folderId, indexId, e);
            throw new SchemaException("Something went wrong while loading schema");
        }
    }

    @Override
    public boolean exists(String folderId, String indexId) {
        return Files.exists(resolveSchemaFile(folderId, indexId));
    }

    // ── private helpers ───────────────────────────────────────────

    private Path resolveFolder(String folderId) {
        return ROOT_PATH.resolve(folderId);
    }

    private Path resolveSchemaFile(String folderId, String indexId) {
        return resolveFolder(folderId).resolve(indexId + "-schema.json");
    }

}