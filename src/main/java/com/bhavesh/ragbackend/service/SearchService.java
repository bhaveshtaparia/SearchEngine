package com.bhavesh.ragbackend.service;

import com.bhavesh.ragbackend.config.LuceneProperties;
import com.bhavesh.ragbackend.dto.SearchHit;
import com.bhavesh.ragbackend.dto.SearchRequest;
import com.bhavesh.ragbackend.dto.SearchResponse;
import com.bhavesh.ragbackend.lucene.exception.LuceneIndexException;
import com.bhavesh.ragbackend.lucene.exception.LuceneSearchException;
import com.bhavesh.ragbackend.utils.LuceneUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final LuceneProperties luceneProperties;
    private final Analyzer analyzer;

    public SearchResponse search(String folderId, String indexId, SearchRequest request) {

        Path indexPath = LuceneUtils.resolvePath(luceneProperties,folderId, indexId);

        if (!Files.exists(indexPath)) {
            throw new LuceneIndexException("Index does not exist: " + indexPath);
        }

        try (FSDirectory directory = FSDirectory.open(indexPath); IndexReader reader = DirectoryReader.open(directory)) {

            IndexSearcher searcher = new IndexSearcher(reader);

            QueryParser queryParser = new QueryParser("content", analyzer);
            Query query = queryParser.parse(request.getQuery());

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
            log.error("Failed to search index at path: {}", indexPath, e);
            throw new LuceneSearchException("Failed to search index");
        } catch (Exception e) {
            log.error("Search query parsing failed for query: {}", request.getQuery(), e);
            throw new LuceneSearchException("Search query parsing failed");
        }
    }
}