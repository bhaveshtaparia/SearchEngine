package com.bhavesh.ragbackend.dto;


import lombok.Data;

import java.util.Map;

@Data
public class IndexDocumentRequest {

    private String documentId;

    private Map<String, DynamicField> fields;
}