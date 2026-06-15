package com.bhavesh.ragbackend.service;

import com.bhavesh.ragbackend.config.LuceneProperties;
import com.bhavesh.ragbackend.dto.schema.FieldDefinition;
import com.bhavesh.ragbackend.dto.search.SearchRequest;
import com.bhavesh.ragbackend.dto.search.SearchResponse;
import com.bhavesh.ragbackend.exception.LuceneSearchException;
import com.bhavesh.ragbackend.lucene.search.IndexSearcherManager;
import com.bhavesh.ragbackend.lucene.search.QueryBuilder;
import com.bhavesh.ragbackend.lucene.search.SearchExecutor;
import com.bhavesh.ragbackend.lucene.search.SearchResultMapper;
import com.bhavesh.ragbackend.utils.LuceneUtils;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final LuceneProperties luceneProperties;
    private final SchemaService schemaService;
    private final QueryBuilder queryBuilder;
    private final IndexSearcherManager searcherManager;
    private final SearchExecutor searchExecutor;
    private final SearchResultMapper resultMapper;

    public SearchResponse search(String folderId, String indexId, SearchRequest request) {
        long startMs = System.currentTimeMillis();

        Path indexPath = LuceneUtils.resolvePath(luceneProperties, folderId, indexId);
        if (!Files.exists(indexPath)) {
            log.warn("Search attempted on non-existent index path={}", indexPath);
            throw new LuceneSearchException(
                    "Index does not exist for folderId=" + folderId + ", indexId=" + indexId +
                            ". Index at least one document first."
            );
        }

        Map<String, FieldDefinition> schema = schemaService.getSchema(folderId, indexId);


        Query query = queryBuilder.build(request, schema);

        IndexSearcher searcher = searcherManager.acquire(folderId, indexId);
        try {
            TopDocs topDocs = searchExecutor.execute(searcher, query, request);

            // Build highlighter only when needed (has CPU cost)
            Highlighter highlighter = request.isHighlight()
                    ? searchExecutor.buildHighlighter(query)
                    : null;

            long tookMs = System.currentTimeMillis() - startMs;

            SearchResponse response = resultMapper.map(
                    searcher, topDocs, request, schema, highlighter, searchExecutor, tookMs
            );

            log.info(
                    "Search complete folderId={} indexId={} query='{}' total={} took={}ms",
                    folderId, indexId, request.getQuery(), response.getTotal(), tookMs
            );

            return response;

        } catch (IOException e) {
            log.error("IO failure during search. folderId={} indexId={}", folderId, indexId, e);
            throw new LuceneSearchException("Search failed due to an internal IO error");
        } finally {

            searcherManager.release(folderId, indexId, searcher);
        }
    }

    /**
     * Refresh the searcher after indexing so new documents are visible.
     */
    public void refreshIndex(String folderId, String indexId) {
        searcherManager.maybeRefresh(folderId, indexId);
    }

    public void removeIndex(String folderId, String indexId) {
        searcherManager.evict(folderId, indexId);
    }
}