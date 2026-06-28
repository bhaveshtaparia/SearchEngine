package com.bhavesh.ragbackend.service;

import com.bhavesh.ragbackend.dto.index.IndexDocumentRequest;
import com.bhavesh.ragbackend.dto.schema.FieldDefinition;
import com.bhavesh.ragbackend.lucene.index.DocumentBuilder;
import com.bhavesh.ragbackend.lucene.index.IndexWriterManager;
import com.bhavesh.ragbackend.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexingServiceTest {

    @Mock
    private DocumentBuilder documentBuilder;

    @Mock
    private IndexWriterManager indexWriterManager;

    @Mock
    private SchemaService schemaService;

    @Mock
    private SearchService searchService;

    @Mock
    private IndexWriter indexWriter;

    @InjectMocks
    private IndexingService indexingService;

    @Test
    void shouldIndexDocumentSuccessfully() throws Exception {

        // Arrange
        String folderId = "folder1";
        String indexId = "index1";

        Map<String, Object> fields = new HashMap<>();
        fields.put("id", "101");
        fields.put("title", "Lucene in Action");

        IndexDocumentRequest request = new IndexDocumentRequest();
        request.setFields(fields);

        // Schema
        FieldDefinition idField = new FieldDefinition();
        idField.setType(LuceneUtils.FieldType.STRING);
        idField.setPrimaryKey(true);

        FieldDefinition titleField = new FieldDefinition();
        titleField.setType(LuceneUtils.FieldType.STRING);

        Map<String, FieldDefinition> schema = new HashMap<>();
        schema.put("id", idField);
        schema.put("title", titleField);

        when(schemaService.getSchema(folderId, indexId))
                .thenReturn(schema);

        when(indexWriterManager.getWriter(folderId, indexId))
                .thenReturn(indexWriter);

        Document document = new Document();

        when(documentBuilder.build(anyString(), anyList()))
                .thenReturn(document);

        // Act
        indexingService.index(folderId, indexId, request);

        // Assert
        verify(schemaService).getSchema(folderId, indexId);

        verify(indexWriterManager).getWriter(folderId, indexId);

        verify(documentBuilder)
                .build(eq("101"), anyList());

        verify(indexWriter)
                .updateDocument(any(Term.class), eq(document));

        verify(indexWriterManager)
                .commit(folderId, indexId);

        verify(indexWriterManager)
                .closeWriter(folderId, indexId);

        verify(searchService)
                .refreshIndex(folderId, indexId);
    }
}