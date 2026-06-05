package com.bhavesh.ragbackend.exception;

public class LuceneIndexException extends RuntimeException {

    public LuceneIndexException(String message, Throwable cause) {
        super(message, cause);
    }

    public LuceneIndexException(String message) {
        super(message);
    }
}