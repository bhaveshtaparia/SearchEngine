package com.bhavesh.ragbackend.controller;

import com.bhavesh.ragbackend.dto.SearchRequest;
import com.bhavesh.ragbackend.dto.SearchResponse;
import com.bhavesh.ragbackend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/folders/{folderId}/indexes/{indexId}")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/search")
    public SearchResponse search(
            @PathVariable String folderId,
            @PathVariable String indexId,
            @RequestBody SearchRequest request
    ) {

        return searchService.search(
                folderId,
                indexId,
                request
        );
    }
}