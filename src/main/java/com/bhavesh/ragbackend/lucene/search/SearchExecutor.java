package com.bhavesh.ragbackend.lucene.search;

import com.bhavesh.ragbackend.dto.SearchRequest;
import com.bhavesh.ragbackend.exception.LuceneSearchException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SearchExecutor {

    private static final Logger log = LoggerFactory.getLogger(SearchExecutor.class);


    private static final int HIGHLIGHT_FRAGMENT_SIZE = 200;

    private final Analyzer analyzer;

    public SearchExecutor(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public TopDocs execute(IndexSearcher searcher, Query query, SearchRequest request) {

        int totalToFetch = request.getFrom() + request.getSize();

        try {
            TopDocs topDocs = searcher.search(query, totalToFetch);

            log.debug(
                    "Query executed: totalHits={}, fetched={}, from={}, size={}",
                    topDocs.totalHits.value(), topDocs.scoreDocs.length,
                    request.getFrom(), request.getSize()
            );

            return topDocs;

        } catch (IOException e) {
            log.error("Failed to execute search query", e);
            throw new LuceneSearchException("Failed to execute search query");
        }
    }

    public Highlighter buildHighlighter(Query query) {
        QueryScorer scorer = new QueryScorer(query);
        Formatter formatter = new SimpleHTMLFormatter("<em>", "</em>");
        Highlighter highlighter = new Highlighter(formatter, scorer);
        highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer, HIGHLIGHT_FRAGMENT_SIZE));
        return highlighter;
    }


    public String highlight(Highlighter highlighter, String fieldName, String fieldValue) {
        if (fieldValue == null || fieldValue.isBlank()) {
            return null;
        }
        try {
            /*
             * TokenStream re-analyzes the fieldValue using the same Analyzer as index time.
             * CRITICAL: the Analyzer used here MUST match the one used during indexing.
             * A mismatch produces wrong token offsets → InvalidTokenOffsetsException
             * or missed highlights.
             */
            TokenStream tokenStream = analyzer.tokenStream(fieldName, fieldValue);
            return highlighter.getBestFragment(tokenStream, fieldValue);
        } catch (InvalidTokenOffsetsException e) {
            log.warn("Highlight token offset mismatch for field='{}'. Check Analyzer consistency.", fieldName);
            return null;
        } catch (IOException e) {
            log.error("Highlight IO failure for field='{}'", fieldName, e);
            return null;
        }
    }
}