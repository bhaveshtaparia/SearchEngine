package com.bhavesh.ragbackend.controller;

import com.bhavesh.ragbackend.annotation.ValidId;
import com.bhavesh.ragbackend.dto.index.BulkIndexRequest;
import com.bhavesh.ragbackend.dto.index.IndexDocumentRequest;
import com.bhavesh.ragbackend.dto.Response;
import com.bhavesh.ragbackend.service.IndexingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/folders/{folderId}/indexes/{indexId}")
@RequiredArgsConstructor
@Validated
public class IndexingController {

    private final IndexingService indexingService;

    @PostMapping("/documents")
    public ResponseEntity<Response> index(
            @PathVariable @ValidId String folderId,
            @PathVariable @ValidId String indexId,
            @RequestBody @Valid IndexDocumentRequest request
    ) {

        indexingService.index(folderId, indexId, request);
        return ResponseEntity.ok(new Response("Document indexed successfully", Response.ResponseType.SUCCESS));
    }

    @PostMapping("/documents/bulk")
    public ResponseEntity<Response> bulkIndex(
            @PathVariable @ValidId String folderId,
            @PathVariable @ValidId String indexId,
            @RequestBody @Valid BulkIndexRequest request
    ) {
        indexingService.bulkIndex(folderId, indexId, request);
        return ResponseEntity.ok(new Response("Documents indexed successfully", Response.ResponseType.SUCCESS));
    }

    @DeleteMapping("/documents")
    public ResponseEntity<Response> deleteDocuments(
            @PathVariable @ValidId String folderId,
            @PathVariable @ValidId String indexId,
            @RequestBody @Valid List<String> documentIds
    ) {
        indexingService.deleteDocuments(folderId, indexId, documentIds);
        return ResponseEntity.ok(new Response("Documents deleted successfully", Response.ResponseType.SUCCESS));
    }

    @DeleteMapping("")
    public ResponseEntity<Response> deleteIndex(
            @PathVariable @ValidId String folderId,
            @PathVariable @ValidId String indexId
    ) {
        indexingService.deleteIndex(folderId, indexId);
        return ResponseEntity.ok(new Response("Index deleted successfully", Response.ResponseType.SUCCESS));
    }

}