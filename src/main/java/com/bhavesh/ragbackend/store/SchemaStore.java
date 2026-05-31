package com.bhavesh.ragbackend.store;

import com.bhavesh.ragbackend.dto.FieldDefinition;

import java.util.Map;

public interface SchemaStore {

    void save(String folderId, String indexId, Map<String, FieldDefinition> schemaDefinition);

    Map<String, FieldDefinition> load(String folderId, String indexId);

    boolean exists(String folderId, String indexId);
}