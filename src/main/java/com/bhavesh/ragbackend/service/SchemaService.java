package com.bhavesh.ragbackend.service;

import com.bhavesh.ragbackend.dto.FieldDefinition;
import com.bhavesh.ragbackend.dto.RegisterSchemaRequest;
import com.bhavesh.ragbackend.exception.SchemaException;
import com.bhavesh.ragbackend.store.SchemaStore;
import com.bhavesh.ragbackend.utils.LuceneUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class SchemaService {
    private final SchemaStore schemaStore;
    private static final Logger log = LoggerFactory.getLogger(SchemaService.class);

    public void registerSchema(String folderId, String indexId, RegisterSchemaRequest request) {
        Map<String, FieldDefinition> schema = request.getFields();
        validatePrimaryKeyField(schema);
        validateSchemaFieldNames(schema);
        if(schemaStore.exists(folderId, indexId)) {
            log.warn("Schema already exits for folderId={}, indexId={}", folderId, indexId);
            throw new SchemaException("Schema already exists for folderId=" + folderId + " and indexId=" + indexId);
        }
        schemaStore.save(folderId, indexId, request.getFields());
    }

    public Map<String, FieldDefinition>  getSchema(String folderId, String indexId) {
        return schemaStore.load(folderId, indexId);
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
            if(LuceneUtils.LUCENE_PRIMARY_KEY_FIELD.equals(fieldName.trim())) {
                throw new SchemaException("Field name " + LuceneUtils.LUCENE_PRIMARY_KEY_FIELD + " is reserved for internal use and cannot be used in schema definition");
            }
        }
    }


}
