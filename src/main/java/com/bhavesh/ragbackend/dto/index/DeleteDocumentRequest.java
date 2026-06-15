package com.bhavesh.ragbackend.dto.index;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DeleteDocumentRequest(
        @NotBlank(message = "document id cannot be blank")
        @NotNull(message = "Provide at least one document id")
        List<String> documentIds) {
}
