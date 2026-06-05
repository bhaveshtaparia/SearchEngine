package com.bhavesh.ragbackend.exception;

public class LuceneSearchException extends RuntimeException {
    public LuceneSearchException(String message) {
        super(message);  // clean user facing message
    }
}