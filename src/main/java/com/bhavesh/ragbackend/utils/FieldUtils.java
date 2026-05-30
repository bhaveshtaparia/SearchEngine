package com.bhavesh.ragbackend.utils;

import com.bhavesh.ragbackend.config.LuceneProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FieldUtils {

    public static enum FieldType {
        STRING,
        INTEGER,
        LONG,
        FLOAT,
        DOUBLE,
        BOOLEAN
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String sanitize(String value) {

        return value.replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    public static String buildKey(String folderId, String indexId) {
        return folderId + ":" + indexId;
    }

    public static Path resolvePath(LuceneProperties properties, String folderId, String indexId) {
        return Paths.get(
                properties.getBasePath(),
                FieldUtils.sanitize(folderId),
                FieldUtils.sanitize(indexId)
        );
    }


}
