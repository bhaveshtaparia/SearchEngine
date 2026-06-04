package com.bhavesh.ragbackend.lucene.exception;

public class LuceneSearchException extends RuntimeException {
    public LuceneSearchException(String message) {
        super(message);  // clean user facing message
    }
}