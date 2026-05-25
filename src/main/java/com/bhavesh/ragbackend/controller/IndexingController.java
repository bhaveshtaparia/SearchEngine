package com.bhavesh.ragbackend.controller;

import com.bhavesh.ragbackend.dto.BulkIndexRequest;
import com.bhavesh.ragbackend.dto.IndexDocumentRequest;
import com.bhavesh.ragbackend.service.IndexingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/folders/{folderId}/indexes/{indexId}")
@RequiredArgsConstructor
public class IndexingController {

    private final IndexingService indexingService;

    @PostMapping("/documents")
    public ResponseEntity<String> index(
            @PathVariable String folderId,
            @PathVariable String indexId,
            @RequestBody IndexDocumentRequest request
    ) {

        indexingService.index(folderId, indexId, request);

        return ResponseEntity.ok("Document indexed successfully");
    }

    @PostMapping("/documents/bulk")
    public ResponseEntity<String> bulkIndex(
            @PathVariable String folderId,
            @PathVariable String indexId,
            @RequestBody BulkIndexRequest request
    ) {

        indexingService.bulkIndex(
                folderId,
                indexId,
                request
        );

        return ResponseEntity.ok("Documents indexed successfully");
    }
}