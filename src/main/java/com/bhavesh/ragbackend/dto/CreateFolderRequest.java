package com.bhavesh.ragbackend.dto;

import com.bhavesh.ragbackend.annotation.ValidId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public record CreateFolderRequest(
        @NotBlank(message = "Folder name cannot be blank")
        @NotNull(message = "Folder name cannot be NULL")
        @ValidId
        String name
) {
}