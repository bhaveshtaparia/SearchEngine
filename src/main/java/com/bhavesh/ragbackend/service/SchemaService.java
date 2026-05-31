package com.bhavesh.ragbackend.service;

import com.bhavesh.ragbackend.dto.FieldDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SchemaService {

    private static final Logger log = LoggerFactory.getLogger(SchemaService.class);

    public void registerSchema(String folderId, String indexId, Map<String, FieldDefinition> schemaDefinition) {

        log.info("Registering schema. folderId={}, indexId={}, schemaDefinition={}", folderId, indexId, schemaDefinition.size());


    }


}
