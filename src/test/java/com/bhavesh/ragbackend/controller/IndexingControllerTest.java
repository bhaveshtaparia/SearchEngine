package com.bhavesh.ragbackend.controller;

import com.bhavesh.ragbackend.dto.Response;
import com.bhavesh.ragbackend.dto.index.IndexDocumentRequest;
import com.bhavesh.ragbackend.service.IndexingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IndexingControllerTest {

    @Mock
    private IndexingService indexingService;

    @InjectMocks
    private IndexingController indexingController;

    @Test
    void shouldIndexDocumentSuccessfully() {

        // Arrange
        String folderId = "folder1";
        String indexId = "index1";


        // Create your request object
        IndexDocumentRequest request = new IndexDocumentRequest();

        // Act
        ResponseEntity<Response> response =
                indexingController.index(folderId, indexId, request);

        // Assert HTTP Status
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Assert Response Body
        assertNotNull(response.getBody());

        assertEquals(
                "Document indexed successfully",
                response.getBody().getMessage()
        );

        assertEquals(
                Response.ResponseType.SUCCESS,
                response.getBody().getType()
        );

        // Verify service call
        verify(indexingService).index(folderId, indexId, request);
    }
}