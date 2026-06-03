package com.bhavesh.ragbackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkIndexRequest {
    @NotNull(message = "Documents must not be null")
    @NotEmpty(message = "At least one document must be provided")
    private List< @Valid IndexDocumentRequest> documents;
}
