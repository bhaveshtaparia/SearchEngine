package com.bhavesh.ragbackend.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IndexWriterManager {

    private final Analyzer analyzer;

    private final ConcurrentHashMap<String, IndexWriter>
            writerCache = new ConcurrentHashMap<>();

    public IndexWriterManager(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public IndexWriter getWriter(
            String folderId,
            String indexId
    ) throws IOException {

        String cacheKey =
                folderId + ":" + indexId;

        if (writerCache.containsKey(cacheKey)) {
            return writerCache.get(cacheKey);
        }

        synchronized (this) {

            if (writerCache.containsKey(cacheKey)) {
                return writerCache.get(cacheKey);
            }

            Path indexPath = Paths.get(
                    "indexes",
                    folderId,
                    indexId
            );

            Files.createDirectories(indexPath);

            FSDirectory directory =
                    FSDirectory.open(indexPath);

            IndexWriterConfig config =
                    new IndexWriterConfig(analyzer);

            IndexWriter writer =
                    new IndexWriter(directory, config);

            writerCache.put(cacheKey, writer);

            return writer;
        }
    }
}