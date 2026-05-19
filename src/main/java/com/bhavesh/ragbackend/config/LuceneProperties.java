package com.bhavesh.ragbackend.config;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "lucene")
@Getter
@Setter
public class LuceneProperties {

    @NotBlank
    private String basePath = "indexes";

    @Positive
    private double ramBufferSizeMb = 256.0;

}