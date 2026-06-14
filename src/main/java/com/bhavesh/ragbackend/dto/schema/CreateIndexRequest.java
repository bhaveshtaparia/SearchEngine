package com.bhavesh.ragbackend.dto.schema;

import com.bhavesh.ragbackend.annotation.ValidId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public record CreateIndexRequest(
        @NotBlank(message = "Index name cannot be blank")
        @NotNull(message = "Index name cannot bu NULL")
        @ValidId
        String name
) {
}