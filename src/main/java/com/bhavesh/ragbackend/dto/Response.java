package com.bhavesh.ragbackend.dto;

import lombok.Getter;

@Getter
public class Response {

    public static enum ResponseType {
        SUCCESS,
        NOT_FOUND
    }
    private final String message;
    private final ResponseType type;
    public Response(String message, ResponseType type) {
        this.message = message;
        this.type = type;
    }
}
