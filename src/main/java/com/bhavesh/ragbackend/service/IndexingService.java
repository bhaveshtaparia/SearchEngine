package com.bhavesh.ragbackend.service;

import com.bhavesh.ragbackend.dto.BulkIndexRequest;
import com.bhavesh.ragbackend.dto.IndexDocumentRequest;
import com.bhavesh.ragbackend.lucene.DocumentBuilder;
import com.bhavesh.ragbackend.lucene.IndexWriterManager;
import com.bhavesh.ragbackend.lucene.exception.LuceneIndexException;
import com.bhavesh.ragbackend.utils.FieldUtils;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingService {

    private static final Logger log = LoggerFactory.getLogger(IndexingService.class);

    private final DocumentBuilder documentBuilder;
    private final IndexWriterManager indexWriterManager;

    public void index(String folderId, String indexId, IndexDocumentRequest request) {

        try {

            validate(folderId, indexId, request);

            Document document = documentBuilder.build(request);

            IndexWriter writer = indexWriterManager.getWriter(folderId, indexId);

            writer.updateDocument(new Term("_documentId", request.getDocumentId()), document);

            indexWriterManager.commit(folderId, indexId);

            log.info("Document indexed successfully. folderId={}, indexId={}, documentId={}", folderId, indexId, request.getDocumentId());

        } catch (Exception ex) {

            throw new LuceneIndexException(String.format("Failed to index document. folderId=%s, indexId=%s, documentId=%s", folderId, indexId, request.getDocumentId()), ex);
        }
    }

    public void bulkIndex(String folderId, String indexId, BulkIndexRequest request) {

        try {

            validate(folderId, indexId, request);
            List<Document> document = documentBuilder.buildBulkDocument(request);

            IndexWriter writer = indexWriterManager.getWriter(folderId, indexId);

            for (Document doc : document) {
                writer.updateDocument(new Term("_documentId", doc.get("_documentId")), doc);
            }

            indexWriterManager.commit(folderId, indexId);

            log.info("Documents indexed successfully. folderId={}, indexId={} ", folderId, indexId);

        } catch (Exception ex) {

            throw new LuceneIndexException(String.format("Failed to index document. folderId=%s, indexId=%s ", folderId, indexId), ex);
        }
    }


    private void validate(String folderId, String indexId, Object request) {

        if (request == null) {
            throw new IllegalArgumentException("Index request cannot be null");
        }

        if (FieldUtils.isBlank(folderId)) {
            throw new IllegalArgumentException("folderId cannot be blank");
        }

        if (FieldUtils.isBlank(indexId)) {
            throw new IllegalArgumentException("indexId cannot be blank");
        }
    }
}