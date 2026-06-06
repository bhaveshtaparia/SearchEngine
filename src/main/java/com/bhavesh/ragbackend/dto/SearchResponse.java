package com.bhavesh.ragbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResponse {
    private List<SearchHit> hits;
    private long total;
    private long tookMs;
}