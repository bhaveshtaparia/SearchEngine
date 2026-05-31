package com.bhavesh.ragbackend.controller;

import com.bhavesh.ragbackend.annotation.ValidId;
import com.bhavesh.ragbackend.dto.RegisterSchemaRequest;
import com.bhavesh.ragbackend.dto.Response;
import com.bhavesh.ragbackend.store.SchemaStore;
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

@RestController
@RequestMapping("/api/v1/folders/{folderId}/indexes/{indexId}")
@RequiredArgsConstructor
@Validated
public class FieldSchemaController {

    private final SchemaStore schemaStore;

    @PostMapping("/schema")
    public ResponseEntity<Response> registerSchema(
            @PathVariable @ValidId String folderId,
            @PathVariable @ValidId String indexId,
            @Valid @RequestBody RegisterSchemaRequest request
    ) {
        schemaStore.save(folderId, indexId, request.getFields());
        return ResponseEntity.ok(new Response("Schema registered successfully", Response.ResponseType.SUCCESS));
    }

    @GetMapping("/schema")
    public ResponseEntity<?> getSchema(
            @PathVariable @ValidId String folderId,
            @PathVariable @ValidId String indexId
    )
    {
        var schema = schemaStore.load(folderId, indexId);
        if (schema == null) {
            return ResponseEntity
                    .status(404)
                    .body(new Response("Schema not found ", Response.ResponseType.NOT_FOUND));
        }
        return ResponseEntity.ok(schema);
    }
}