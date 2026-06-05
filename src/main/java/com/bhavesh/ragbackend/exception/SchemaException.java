package com.bhavesh.ragbackend.exception;

public class SchemaException extends RuntimeException {
    public SchemaException(String message) {
        super(message);  // clean user facing message
    }
}
