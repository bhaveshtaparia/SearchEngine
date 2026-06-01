package com.bhavesh.ragbackend.store;

import com.bhavesh.ragbackend.dto.FieldDefinition;
import com.bhavesh.ragbackend.lucene.exception.SchemaException;
import com.bhavesh.ragbackend.utils.FieldUtils;
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
            validatePrimaryKeyField(schema);
            validateSchemaFieldNames(schema);
            if(exists(folderId, indexId)) {
                log.warn("Schema already exits for folderId={}, indexId={}", folderId, indexId);
                throw new SchemaException("Schema already exists for folderId=" + folderId + " and indexId=" + indexId);
            }
            Path folder = resolveFolder(folderId);

            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(resolveSchemaFile(folderId, indexId).toFile(), schema);

        }
        catch (SchemaException e) {
            log.error("Schema error for folderId={}, indexId={}: {}", folderId, indexId, e.getMessage());
            throw e;
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

    private void validatePrimaryKeyField(Map<String, FieldDefinition> schema) {
        String primaryKeyField = null;
        for (var entry : schema.entrySet()) {
            if (entry.getValue().isPrimaryKey()) {
                if (primaryKeyField != null) {
                    throw new SchemaException("Multiple primary key fields defined, There should be only one primary key field in the schema");
                }
                primaryKeyField = entry.getKey();
            }
        }
        if (primaryKeyField == null) {
            throw new SchemaException("No primary key field defined, There should be one primary key field in the schema");
        }
    }
    private void validateSchemaFieldNames(Map<String, FieldDefinition> schema) {
        for (String fieldName : schema.keySet()) {
            if (!fieldName.matches("^[a-zA-Z0-9_-]+$")) {
                throw new SchemaException("Invalid field name: " + fieldName + ". Field names can only contain letters, numbers, underscores, and hyphens.");
            }
            if(FieldUtils.LUCENE_PRIMARY_KEY_FIELD.equals(fieldName.trim())) {
                throw new SchemaException("Field name " + FieldUtils.LUCENE_PRIMARY_KEY_FIELD + " is reserved for internal use and cannot be used in schema definition");
            }
        }
    }
}