package com.bhavesh.ragbackend.controller;

import com.bhavesh.ragbackend.annotation.ValidId;
import com.bhavesh.ragbackend.dto.BulkIndexRequest;
import com.bhavesh.ragbackend.dto.IndexDocumentRequest;
import com.bhavesh.ragbackend.dto.Response;
import com.bhavesh.ragbackend.service.IndexingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        indexingService.bulkIndex(
                folderId,
                indexId,
                request
        );
        return ResponseEntity.ok(new Response("Document indexed successfully", Response.ResponseType.SUCCESS));
    }
}