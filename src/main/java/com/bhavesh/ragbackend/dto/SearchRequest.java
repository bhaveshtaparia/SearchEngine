package com.bhavesh.ragbackend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SearchRequest {


    @NotNull(message = "Query must not be null")
    @NotEmpty(message = "Query must not be empty")
    private String query;

    private List<String> searchFields;

    private Map<String, String> filters;

    @Min(value = 0, message = "from must be >= 0")
    private int from = 0;

    @Min(value = 1, message = "size must be >= 1")
    @Max(value = 100, message = "size must be <= 100")
    private int size = 10;

    private List<String> returnFields;

    private boolean highlight = false;
}