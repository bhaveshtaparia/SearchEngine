package com.bhavesh.ragbackend.service;

import com.bhavesh.ragbackend.dto.BulkIndexRequest;
import com.bhavesh.ragbackend.dto.FieldDefinition;
import com.bhavesh.ragbackend.dto.IndexDocumentRequest;
import com.bhavesh.ragbackend.lucene.DocumentBuilder;
import com.bhavesh.ragbackend.lucene.IndexWriterManager;
import com.bhavesh.ragbackend.lucene.exception.LuceneIndexException;
import com.bhavesh.ragbackend.model.IndexField;
import com.bhavesh.ragbackend.utils.LuceneUtils;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndexingService {

    private static final Logger log = LoggerFactory.getLogger(IndexingService.class);

    private final DocumentBuilder documentBuilder;
    private final IndexWriterManager indexWriterManager;
    private final SchemaService schemaService;

    public void index(String folderId, String indexId, IndexDocumentRequest request) {
            Map<String, List<IndexField>> field = getFields(folderId, indexId, request);
            indexDocument(folderId, indexId, field);
    }

    public void bulkIndex(String folderId, String indexId, BulkIndexRequest request) {
        Map<String, List<IndexField>> field = getFields(folderId, indexId, request);
        indexDocument(folderId, indexId, field);
    }

    private Map<String, List<IndexField>> getFields(String folderId, String indexId, IndexDocumentRequest request) {
        Map<String, List<IndexField>> primaryKeyVsIndexFields = new HashMap<>();
        Map<String, Object> fields = request.getFields();
        Map<String, FieldDefinition> fieldsSchema = schemaService.getSchema(folderId, indexId);
        String primaryKeyValue = null;
        List<IndexField> indexFields = new ArrayList<>();
        primaryKeyValue = getIndexField(fields, fieldsSchema, indexFields);
        primaryKeyVsIndexFields.put(primaryKeyValue, indexFields);
        return primaryKeyVsIndexFields;
    }

    private Map<String, List<IndexField>> getFields(String folderId, String indexId, BulkIndexRequest request) {
        Map<String, List<IndexField>> primaryKeyVsIndexFields = new HashMap<>();
        Map<String, FieldDefinition> fieldsSchema = schemaService.getSchema(folderId, indexId);
        for(IndexDocumentRequest documentRequest : request.getDocuments()) {
            Map<String, Object> fields = documentRequest.getFields();
            String primaryKeyValue = null;
            List<IndexField> indexFields = new ArrayList<>();
            primaryKeyValue = getIndexField(fields, fieldsSchema, indexFields);
            primaryKeyVsIndexFields.put(primaryKeyValue, indexFields);
        }
        return primaryKeyVsIndexFields;
    }

    private String getIndexField(Map<String, Object> fields, Map<String, FieldDefinition> fieldsSchema, List<IndexField> indexFields) {
        String primaryKeyValue = null;
        for (var entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();
            FieldDefinition fieldDefinition = fieldsSchema.get(fieldName);
            if (fieldDefinition == null) {
                log.warn("Field {} is not defined in schema, skipping it", fieldName);
                continue;
            }
            IndexField indexField = new IndexField(fieldName, fieldValue, fieldDefinition.getType());
            indexField.consumeFieldDefinition(fieldDefinition);
            indexFields.add(indexField);
            if (fieldDefinition.isPrimaryKey()) {
                if (primaryKeyValue != null) {
                    throw new LuceneIndexException("Multiple primary key values found in document fields, there should be only one primary key field in the schema");
                }
                primaryKeyValue = (String) indexField.getValue();
            }
        }
        return primaryKeyValue;
    }

    private void indexDocument(String folderId, String indexId, Map<String, List<IndexField>> field) {
        IndexWriter writer = indexWriterManager.getWriter(folderId, indexId);
        try {
            for (Map.Entry<String, List<IndexField>> entry : field.entrySet()) {
                String fieldValue = entry.getKey();
                List<IndexField> indexFields = entry.getValue();
                Document doc = documentBuilder.build(fieldValue, indexFields);
                writer.updateDocument(new Term(LuceneUtils.LUCENE_PRIMARY_KEY_FIELD, fieldValue), doc);
            }
        }
        catch (LuceneIndexException | IllegalArgumentException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            log.error("Failed to build Lucene document for indexing. folderId={}, indexId={}, error={}", folderId, indexId, ex.getMessage(), ex);
            throw new LuceneIndexException("Failed to build Lucene document for indexing");
        }
        log.info("Document indexed successfully. folderId={}, indexId={}", folderId, indexId);
        indexWriterManager.commit(folderId, indexId);
    }
}