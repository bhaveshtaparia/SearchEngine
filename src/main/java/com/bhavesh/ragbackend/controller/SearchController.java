package com.bhavesh.ragbackend.controller;

import com.bhavesh.ragbackend.annotation.ValidId;
import com.bhavesh.ragbackend.dto.SearchRequest;
import com.bhavesh.ragbackend.dto.SearchResponse;
import com.bhavesh.ragbackend.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/folders/{folderId}/indexes/{indexId}")
@RequiredArgsConstructor
@Validated
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/search")
    public SearchResponse search(
            @PathVariable @ValidId String folderId,
            @PathVariable @ValidId String indexId,
            @RequestBody @Valid SearchRequest request
    ) {

        return searchService.search(
                folderId,
                indexId,
                request
        );
    }
}