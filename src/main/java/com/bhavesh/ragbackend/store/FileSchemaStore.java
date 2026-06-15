package com.bhavesh.ragbackend.store;

import com.bhavesh.ragbackend.dto.schema.FieldDefinition;
import com.bhavesh.ragbackend.exception.SchemaException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FileSchemaStore implements SchemaStore {

    private static final Path ROOT_PATH = Paths.get("data/schemas");
    private static final Logger log = LoggerFactory.getLogger(FileSchemaStore.class);

    private final ObjectMapper objectMapper;

    @Override
    public void createFolder(String folderId) {
        Path folder = resolveFolder(folderId);

        try {
            if (Files.exists(folder)) {
                log.warn("Folder id already exits for folder {}",folderId);
                throw new SchemaException("Folder with this name already exits");
            }

            Files.createDirectories(folder);
        }
        catch (SchemaException ex){
            throw ex;
        }
        catch (Exception ex){
            log.error("Error creating a Folder {}",folder,ex);
            throw new SchemaException("Something went wrong while creating a Folder");
        }
    }

    @Override
    public List<String> getFolders() {
        try {
            if (!Files.exists(ROOT_PATH)) {
                return List.of();
            }

            try (var paths = Files.list(ROOT_PATH)) {
                return paths
                        .filter(Files::isDirectory)
                        .map(path -> path.getFileName().toString())
                        .toList();
            }
        } catch (Exception ex) {
            log.error("Error fetching folders", ex);
            throw new SchemaException("Something went wrong while fetching folders");
        }
    }

    @Override
    public void createIndex(String folderId, String indexId) {
        try {
            Path folder = resolveFolder(folderId);

            if (!Files.exists(folder)) {
                log.warn("Folder does not exist for folderId {}", folderId);
                throw new SchemaException("Folder does not exist");
            }

            if (placeExists(folderId, indexId)) {
                log.warn("Index already exists for indexId {}", indexId);
                throw new SchemaException("Index already exists");
            }

            Files.createFile(resolveSchemaFile(folderId, indexId));

        } catch (SchemaException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error creating index {}", indexId, ex);
            throw new SchemaException("Something went wrong while creating index");
        }
    }

    @Override
    public List<String> getIndexes(String folderId) {
        try {
            Path folder = resolveFolder(folderId);

            if (!Files.exists(folder)) {
                return List.of();
            }

            try (var paths = Files.list(folder)) {
                return paths
                        .filter(Files::isRegularFile)
                        .map(path -> path.getFileName().toString())
                        .filter(name -> name.endsWith("-schema.json"))
                        .map(name -> name.substring(
                                0,
                                name.length() - "-schema.json".length()
                        ))
                        .toList();
            }
        } catch (Exception ex) {
            log.error("Error fetching indexes for folder {}", folderId, ex);
            throw new SchemaException("Something went wrong while fetching indexes");
        }
    }

    @Override
    public void save(String folderId, String indexId, Map<String, FieldDefinition> schema) {

        Path folder = resolveFolder(folderId);
        Path schemaFile = resolveSchemaFile(folderId, indexId);

        try {
            if (!Files.exists(folder)) {
                throw new SchemaException("Folder does not exist");
            }

            if (!Files.exists(schemaFile)) {
                throw new SchemaException("Index does not exist");
            }

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(schemaFile.toFile(), schema);

        } catch (SchemaException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error saving schema for folderId={}, indexId={}",
                    folderId, indexId, ex);

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

            if (Files.size(schemaFile) == 0) {
                return Map.of();
            }
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
    public boolean hasSchema(String folderId, String indexId) {
        Map<String, FieldDefinition> schema = load(folderId, indexId);
        return schema != null && !schema.isEmpty();
    }

    @Override
    public void deleteFolder(String folderId) {
        Path folder = resolveFolder(folderId);

        try {
            if (!Files.exists(folder)) {
                throw new SchemaException("Folder doesn't exists");
            }

            Files.walk(folder)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

        }catch (SchemaException ex){
            throw ex;
        }
        catch (Exception ex) {
            log.error("Error deleting folder {}", folderId, ex);
            throw new SchemaException(
                    "Something went wrong while deleting folder");
        }
    }

    @Override
    public void deleteIndex(String folderId, String indexId) {
        Path schemaFile = resolveSchemaFile(folderId, indexId);

        try {
            if (!Files.exists(schemaFile)) {
                throw new SchemaException("Index doesn't exits");
            }

            Files.delete(schemaFile);

        }catch (SchemaException ex){
            throw ex;
        }
        catch (Exception ex) {
            log.error(
                    "Error deleting schema for folderId={}, indexId={}",
                    folderId,
                    indexId,
                    ex
            );

            throw new SchemaException(
                    "Something went wrong while deleting index schema");
        }
    }


    private Path resolveFolder(String folderId) {
        return ROOT_PATH.resolve(folderId);
    }

    private Path resolveSchemaFile(String folderId, String indexId) {
        return resolveFolder(folderId).resolve(indexId + "-schema.json");
    }


    private boolean placeExists(String folderId, String indexId) {
        return Files.exists(resolveSchemaFile(folderId, indexId));
    }


}