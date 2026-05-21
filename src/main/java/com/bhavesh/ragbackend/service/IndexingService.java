package com.bhavesh.ragbackend.service;

import com.bhavesh.ragbackend.dto.DynamicIndexDocumentRequest;
import com.bhavesh.ragbackend.lucene.DocumentBuilder;
import com.bhavesh.ragbackend.lucene.IndexWriterManager;
import com.bhavesh.ragbackend.lucene.exception.LuceneIndexException;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexingService {

    private static final Logger log = LoggerFactory.getLogger(IndexingService.class);

    private final DocumentBuilder documentBuilder;
    private final IndexWriterManager indexWriterManager;

    public void index(DynamicIndexDocumentRequest request) {

        try {

            Document document = documentBuilder.build(request);

            IndexWriter writer = indexWriterManager.getWriter(request.getFolderId(), request.getIndexId());

            writer.updateDocument(new Term("_documentId", request.getDocumentId()), document);

            indexWriterManager.commit(request.getFolderId(), request.getIndexId());

            log.info("Document indexed successfully. folderId={}, indexId={}, documentId={}", request.getFolderId(), request.getIndexId(), request.getDocumentId());

        } catch (Exception ex) {

            throw new LuceneIndexException(String.format("Failed to index document. folderId=%s, indexId=%s, documentId=%s", request.getFolderId(), request.getIndexId(), request.getDocumentId()), ex);
        }
    }
}