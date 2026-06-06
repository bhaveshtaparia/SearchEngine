package com.bhavesh.ragbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SearchHit {
    private String documentId;
    private float score;
    private Map<String, Object> fields;
    private Map<String, String> highlights;
}