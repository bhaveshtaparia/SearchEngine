package com.bhavesh.ragbackend.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class IndexDocumentRequest {

    @NotNull(message = "Fields must not be null")
    @NotEmpty(message = "At least one field must be defined")
    private Map<String, Object> fields;
}