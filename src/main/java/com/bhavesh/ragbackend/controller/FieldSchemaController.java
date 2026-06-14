package com.bhavesh.ragbackend.controller;

import com.bhavesh.ragbackend.annotation.ValidId;
import com.bhavesh.ragbackend.dto.CreateFolderRequest;
import com.bhavesh.ragbackend.dto.CreateIndexRequest;
import com.bhavesh.ragbackend.dto.FieldDefinition;
import com.bhavesh.ragbackend.dto.RegisterSchemaRequest;
import com.bhavesh.ragbackend.dto.Response;
import com.bhavesh.ragbackend.service.SchemaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/folders")
@RequiredArgsConstructor
@Validated
public class FieldSchemaController {

    private final SchemaService schemaService;

    @PostMapping
    public ResponseEntity<Response> createFolder(@Valid @RequestBody CreateFolderRequest request) {
        schemaService.createFolder(request);

        return ResponseEntity.ok(new Response("Folder created successfully", Response.ResponseType.SUCCESS));
    }

    @GetMapping("")
    public ResponseEntity<?> getFolder() {
        List<String> folder = schemaService.getFolders();

        if (folder == null) {
            return ResponseEntity.status(404).body(new Response("Folder not found", Response.ResponseType.NOT_FOUND));
        }

        return ResponseEntity.ok(folder);
    }


    @PostMapping("/{folderId}/indexes")
    public ResponseEntity<Response> createIndex(@PathVariable @ValidId String folderId,
                                                @Valid @RequestBody CreateIndexRequest request) {
        schemaService.createIndex(folderId, request);

        return ResponseEntity.ok(new Response("Index created successfully", Response.ResponseType.SUCCESS));
    }

    @GetMapping("/{folderId}/indexes")
    public ResponseEntity<?> getIndex(@PathVariable @ValidId String folderId) {
        List<String> index = schemaService.getIndexes(folderId);

        if (index == null) {
            return ResponseEntity.status(404).body(new Response("Index not found", Response.ResponseType.NOT_FOUND));
        }

        return ResponseEntity.ok(index);
    }


    @PostMapping("/{folderId}/indexes/{indexId}/schema")
    public ResponseEntity<Response> registerSchema(@PathVariable @ValidId String folderId,
                                                   @PathVariable @ValidId String indexId,
                                                   @Valid @RequestBody RegisterSchemaRequest request) {
        schemaService.registerSchema(folderId, indexId, request);
        return ResponseEntity.ok(new Response("Schema registered successfully", Response.ResponseType.SUCCESS));
    }

    @GetMapping("/{folderId}/indexes/{indexId}/schema")
    public ResponseEntity<?> getSchema(@PathVariable @ValidId String folderId,
                                       @PathVariable @ValidId String indexId) {
        Map<String, FieldDefinition> schema = schemaService.getSchema(folderId, indexId);
        if (schema == null) {
            return ResponseEntity
                    .status(404)
                    .body(new Response("Schema not found ", Response.ResponseType.NOT_FOUND));
        }
        return ResponseEntity.ok(schema);
    }
}