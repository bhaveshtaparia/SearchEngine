package com.bhavesh.ragbackend.service;

import com.bhavesh.ragbackend.config.LuceneProperties;
import com.bhavesh.ragbackend.dto.SearchHit;
import com.bhavesh.ragbackend.dto.SearchRequest;
import com.bhavesh.ragbackend.dto.SearchResponse;
import com.bhavesh.ragbackend.lucene.exception.LuceneIndexException;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final LuceneProperties luceneProperties;

    public SearchResponse search(String folderId, String indexId, SearchRequest request) {

        validateRequest(folderId, indexId, request);

        Path indexPath = buildIndexPath(folderId, indexId);

        if (!Files.exists(indexPath)) {
            throw new LuceneIndexException("Index does not exist: " + indexPath);
        }

        try (FSDirectory directory = FSDirectory.open(indexPath); IndexReader reader = DirectoryReader.open(directory)) {

            IndexSearcher searcher = new IndexSearcher(reader);

            /*
             IMPORTANT:
             content = field name you want to search

             Change this later dynamically.
            */
            Query query = new TermQuery(new Term("content", request.getQuery().toLowerCase()));

            TopDocs topDocs = searcher.search(query, 10);

            List<SearchHit> hits = new ArrayList<>();

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {

                Document document = searcher.storedFields().document(scoreDoc.doc);

                SearchHit hit = new SearchHit();

                hit.setDocumentId(document.get("_documentId"));

                hit.setScore(scoreDoc.score);

                Map<String, Object> fields = new HashMap<>();

                for (IndexableField field : document.getFields()) {

                    String fieldName = field.name();

                    /*
                     Skip internal system field
                    */
                    if ("_documentId".equals(fieldName)) {
                        continue;
                    }

                    /*
                     Avoid duplicate values
                    */
                    if (!fields.containsKey(fieldName)) {
                        fields.put(fieldName, document.get(fieldName));
                    }
                }

                hit.setFields(fields);

                hits.add(hit);
            }

            SearchResponse response = new SearchResponse();

            response.setHits(hits);

            return response;

        } catch (IOException e) {

            throw new LuceneIndexException("Failed to search index", e);
        }
    }

    private void validateRequest(String folderId, String indexId, SearchRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        if (isBlank(folderId)) {
            throw new IllegalArgumentException("folderId cannot be blank");
        }

        if (isBlank(indexId)) {
            throw new IllegalArgumentException("indexId cannot be blank");
        }

        if (isBlank(request.getQuery())) {
            throw new IllegalArgumentException("query cannot be blank");
        }
    }

    private Path buildIndexPath(String folderId, String indexId) {

        return Path.of(luceneProperties.getBasePath(), sanitize(folderId), sanitize(indexId));
    }

    private String sanitize(String value) {

        return value.replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    private boolean isBlank(String value) {

        return value == null || value.trim().isEmpty();
    }
}