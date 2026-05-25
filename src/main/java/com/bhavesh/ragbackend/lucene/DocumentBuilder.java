package com.bhavesh.ragbackend.lucene;

import com.bhavesh.ragbackend.dto.BulkIndexRequest;
import com.bhavesh.ragbackend.dto.DynamicField;
import com.bhavesh.ragbackend.dto.IndexDocumentRequest;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DocumentBuilder {

    private static final Logger log = LoggerFactory.getLogger(DocumentBuilder.class);

    private final LuceneFieldMapper luceneFieldMapper;

    public Document build(IndexDocumentRequest request) {

        if(request.getDocumentId()==null || request.getDocumentId().trim().isEmpty()) {
            throw new IllegalArgumentException("Document ID cannot be empty");
        }

        Document document = new Document();

        /*
            Required system field
            Used for upsert/updateDocument()
        */
        document.add(new StringField("_documentId", request.getDocumentId(), Field.Store.YES));


        Map<String, DynamicField> fields = request.getFields();

        if (fields == null || fields.isEmpty()) {
            log.warn("Document contains no dynamic fields. documentId={}", request.getDocumentId());

            return document;
        }

        for (Map.Entry<String, DynamicField> entry : fields.entrySet()) {

            String fieldName = entry.getKey();
            DynamicField dynamicField = entry.getValue();

            List<IndexableField> luceneFields = luceneFieldMapper.mapField(fieldName, dynamicField);

            luceneFields.forEach(document::add);
        }

        return document;
    }

    public List<Document> buildBulkDocument(BulkIndexRequest request) {

        List<Document> documents = request.getDocuments().stream()
                .map(this::build)
                .toList();

        return documents;
    }

}