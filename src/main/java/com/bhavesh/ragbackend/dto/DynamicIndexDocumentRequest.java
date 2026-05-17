package com.bhavesh.ragbackend.dto;


import lombok.Data;

import java.util.Map;

@Data
public class DynamicIndexDocumentRequest {

    private String folderId;

    private String indexId;

    private String documentId;

    private Map<String, DynamicField> fields;
}