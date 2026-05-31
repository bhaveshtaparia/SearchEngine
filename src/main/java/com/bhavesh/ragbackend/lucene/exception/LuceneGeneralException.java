package com.bhavesh.ragbackend.lucene.exception;

public class LuceneGeneralException extends RuntimeException {
    public LuceneGeneralException(String message) {
        super(message);  // clean user facing message
    }
}