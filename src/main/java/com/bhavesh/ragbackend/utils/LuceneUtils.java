package com.bhavesh.ragbackend.utils;

import com.bhavesh.ragbackend.config.LuceneProperties;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class LuceneUtils {

    public static enum FieldType {
        STRING,
        INTEGER,
        LONG,
        FLOAT,
        DOUBLE,
        BOOLEAN
    }

    public static String LUCENE_PRIMARY_KEY_FIELD = "_documentId";

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
                sanitize(folderId),
                sanitize(indexId)
        );
    }

    public static Object getPrimaryKeyFieldValue(Map<String, Object> document) {
        return  null;
    }


}
