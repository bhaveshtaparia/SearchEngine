package com.bhavesh.ragbackend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class SearchRequest {

    @NotNull(message = "Query must not be null")
    @NotEmpty(message = "Query must not be empty")
    private String query;
    int topK = 5;
    Set<String> fields;
}