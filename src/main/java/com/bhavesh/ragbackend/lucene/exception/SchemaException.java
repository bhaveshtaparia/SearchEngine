package com.bhavesh.ragbackend.lucene.exception;

public class SchemaException extends RuntimeException {
    public SchemaException(String message) {
        super(message);  // clean user facing message
    }
}
