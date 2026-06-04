package com.bhavesh.ragbackend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SearchRequest {

    @NotNull(message = "Query must not be null")
    @NotEmpty(message = "Query must not be empty")
    private String query;

}