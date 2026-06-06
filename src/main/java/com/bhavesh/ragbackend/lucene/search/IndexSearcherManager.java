package com.bhavesh.ragbackend.lucene.search;

import com.bhavesh.ragbackend.config.LuceneProperties;
import com.bhavesh.ragbackend.exception.LuceneSearchException;
import com.bhavesh.ragbackend.utils.LuceneUtils;
import jakarta.annotation.PreDestroy;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IndexSearcherManager {

    private static final Logger log = LoggerFactory.getLogger(IndexSearcherManager.class);

    private final LuceneProperties luceneProperties;


    private final ConcurrentHashMap<String, SearcherManager> managerCache = new ConcurrentHashMap<>();

    public IndexSearcherManager(LuceneProperties luceneProperties) {
        this.luceneProperties = luceneProperties;
    }


    public IndexSearcher acquire(String folderId, String indexId) {
        String key = LuceneUtils.buildKey(folderId, indexId);
        SearcherManager manager = managerCache.computeIfAbsent(key, k ->
                createSearcherManager(folderId, indexId)
        );
        try {
            return manager.acquire();
        } catch (IOException e) {
            log.error("Failed to acquire IndexSearcher for key={}", key, e);
            throw new LuceneSearchException(
                   "Something went wrong while searching , please try again later."
            );
        }
    }

    public void release(String folderId, String indexId, IndexSearcher searcher) {
        String key = LuceneUtils.buildKey(folderId, indexId);
        SearcherManager manager = managerCache.get(key);
        if (manager == null) {
            log.warn("release() called but no SearcherManager found for key={}", key);
            return;
        }
        try {
            manager.release(searcher);
        } catch (IOException e) {
            log.error("Failed to release IndexSearcher for key={}", key, e);
        }
    }


    public void maybeRefresh(String folderId, String indexId) {
        String key = LuceneUtils.buildKey(folderId, indexId);
        SearcherManager manager = managerCache.get(key);
        if (manager == null) {
            // Index was never searched — no searcher to refresh. That's fine.
            return;
        }
        try {
            boolean refreshed = manager.maybeRefresh();
            if (refreshed) {
                log.debug("SearcherManager refreshed for key={}", key);
            }
        } catch (IOException e) {
            log.error("Failed to refresh SearcherManager for key={}", key, e);
        }
    }


    public void evict(String folderId, String indexId) {
        String key = LuceneUtils.buildKey(folderId, indexId);
        SearcherManager manager = managerCache.remove(key);
        if (manager != null) {
            closeQuietly(manager, key);
        }
    }


    @PreDestroy
    public void shutdown() {
        log.info("Shutting down IndexSearcherManager, closing {} searcher(s)", managerCache.size());
        managerCache.forEach((key, manager) -> closeQuietly(manager, key));
        managerCache.clear();
    }

    private SearcherManager createSearcherManager(String folderId, String indexId) {
        Path indexPath = LuceneUtils.resolvePath(luceneProperties, folderId, indexId);
        try {
            FSDirectory directory = FSDirectory.open(indexPath);
            SearcherManager manager = new SearcherManager(directory, new SearcherFactory());
            log.info("Created SearcherManager for key={}:{}", folderId, indexId);
            return manager;
        } catch (IOException e) {
            log.error("Failed to create SearcherManager for path={}", indexPath, e);
            throw new LuceneSearchException("Something went wrong while searching , please try again later.");
        }
    }

    private void closeQuietly(SearcherManager manager, String key) {
        try {
            manager.close();
            log.debug("Closed SearcherManager for key={}", key);
        } catch (IOException e) {
            log.error("Error closing SearcherManager for key={}", key, e);
        }
    }
}