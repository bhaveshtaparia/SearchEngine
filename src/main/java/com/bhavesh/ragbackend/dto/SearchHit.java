package com.bhavesh.ragbackend.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SearchHit {

    private String documentId;

    private float score;

    private Map<String, Object> fields;

}