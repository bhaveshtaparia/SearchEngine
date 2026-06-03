package com.bhavesh.ragbackend.lucene;

import com.bhavesh.ragbackend.config.LuceneProperties;
import com.bhavesh.ragbackend.lucene.exception.LuceneIndexException;
import com.bhavesh.ragbackend.utils.LuceneUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IndexWriterManager {

    private static final Logger log =
            LoggerFactory.getLogger(IndexWriterManager.class);

    private final Analyzer analyzer;
    private final LuceneProperties properties;

    private final Map<String, IndexWriter> writerCache =
            new ConcurrentHashMap<>();

    public IndexWriterManager(Analyzer analyzer, LuceneProperties properties) {
        this.analyzer = analyzer;
        this.properties = properties;
    }

    public IndexWriter getWriter(String folderId, String indexId) {

        String key = LuceneUtils.buildKey(folderId, indexId);

        return writerCache.compute(key, (k, existingWriter) -> {

            if (existingWriter != null && isValid(existingWriter)) {
                log.info("Reusing IndexWriter: {}", k);
                return existingWriter;
            }

            if (existingWriter != null) {
                log.warn("Stale IndexWriter detected, recreating: {}", k);
                safeClose(k, existingWriter);
            }

            return createWriter(folderId, indexId);
        });
    }

    public void commit(String folderId, String indexId) {

        String key = LuceneUtils.buildKey(folderId, indexId);
        IndexWriter writer = writerCache.get(key);

        if (writer == null || !writer.isOpen()) {
            log.warn("Commit skipped. Writer not found or closed: {}", key);
            return;
        }

        try {
            writer.commit();
            log.info("Committed IndexWriter: {}", key);

        } catch (Exception e) {
            log.error("Commit failed for key: {}", key, e);
            throw new LuceneIndexException("Commit failed for: " + key, e);
        }
    }

    public void closeWriter(String folderId, String indexId) {

        String key = LuceneUtils.buildKey(folderId, indexId);
        IndexWriter writer = writerCache.remove(key);

        if (writer != null) {
            safeClose(key, writer);
            log.info("Closed IndexWriter: {}", key);
        }
    }

    @PreDestroy
    public void shutdown() {

        log.info("Shutting down IndexWriterManager. Writers={}",
                writerCache.size());

        writerCache.forEach(this::safeClose);
        writerCache.clear();

        log.info("IndexWriterManager shutdown complete.");
    }

    private IndexWriter createWriter(String folderId, String indexId) {

        try {
            Path indexPath = LuceneUtils.resolvePath(properties, folderId, indexId);
            Files.createDirectories(indexPath);

            FSDirectory directory = FSDirectory.open(indexPath);

            IndexWriterConfig config =
                    new IndexWriterConfig(analyzer);

            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            config.setRAMBufferSizeMB(properties.getRamBufferSizeMb());

            IndexWriter writer = new IndexWriter(directory, config);

            log.info("Created IndexWriter at: {}", indexPath);

            return writer;

        } catch (IOException e) {

            log.error(
                    "Failed to create IndexWriter for: {}/{}",
                    folderId,
                    indexId,
                    e
            );

            throw new LuceneIndexException(
                    "Failed to create IndexWriter for: " + folderId + "/" + indexId,
                    e
            );
        }
    }

    private boolean isValid(IndexWriter writer) {
        return writer != null && writer.isOpen();
    }

    private void safeClose(String key, IndexWriter writer) {

        try {
            writer.commit();

        } catch (Exception e) {
            log.warn(
                    "Commit failed during close for '{}', closing anyway",
                    key,
                    e
            );
        } finally {
            try {
                writer.close();

            } catch (Exception e) {
                log.error("Failed to close writer: {}", key, e);
            }
        }
    }
}