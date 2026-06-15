package com.bhavesh.ragbackend.store;

import com.bhavesh.ragbackend.dto.schema.FieldDefinition;

import java.util.List;
import java.util.Map;

public interface SchemaStore {

    void createFolder(String folderId);

    List<String> getFolders();

    void createIndex(String folderId, String indexId);

    List<String> getIndexes(String folderId);

    void save(String folderId, String indexId, Map<String, FieldDefinition> schemaDefinition);

    Map<String, FieldDefinition> load(String folderId, String indexId);

    boolean hasSchema(String folderId, String indexId);

    void deleteFolder(String folderId);

    void deleteIndex(String folderId,String indexId);
}