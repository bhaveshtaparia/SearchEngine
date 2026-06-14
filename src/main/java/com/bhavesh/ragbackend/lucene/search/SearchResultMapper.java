package com.bhavesh.ragbackend.lucene.search;

import com.bhavesh.ragbackend.dto.schema.FieldDefinition;
import com.bhavesh.ragbackend.dto.search.SearchHit;
import com.bhavesh.ragbackend.dto.search.SearchRequest;
import com.bhavesh.ragbackend.dto.search.SearchResponse;
import com.bhavesh.ragbackend.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;


@Component
public class SearchResultMapper {

    private static final Logger log = LoggerFactory.getLogger(SearchResultMapper.class);


    public SearchResponse map(
            IndexSearcher searcher,
            TopDocs topDocs,
            SearchRequest request,
            Map<String, FieldDefinition> schema,
            Highlighter highlighter,
            SearchExecutor executor,
            long tookMs
    ) throws IOException {

        ScoreDoc[] scoreDocs = topDocs.scoreDocs;


        int start = Math.min(request.getFrom(), scoreDocs.length);
        int end   = Math.min(request.getFrom() + request.getSize(), scoreDocs.length);


        Set<String> returnFieldSet = request.getReturnFields() != null
                ? new HashSet<>(request.getReturnFields())
                : null;

        List<SearchHit> hits = new ArrayList<>(end - start);

        for (int i = start; i < end; i++) {
            ScoreDoc scoreDoc = scoreDocs[i];
            Document document = loadDocument(searcher, scoreDoc.doc, returnFieldSet);
            SearchHit hit = mapHit(document, scoreDoc, schema, request, highlighter, executor, returnFieldSet);
            hits.add(hit);
        }

        return SearchResponse.builder()
                .hits(hits)
                .total(topDocs.totalHits.value())
                .tookMs(tookMs)
                .build();
    }


    private Document loadDocument(IndexSearcher searcher,
                                  int docId,
                                  Set<String> returnFieldSet) throws IOException {
        if (returnFieldSet == null) {
            // Load everything
            return searcher.storedFields().document(docId);
        }

        Set<String> fieldsToLoad = new HashSet<>(returnFieldSet);
        fieldsToLoad.add(LuceneUtils.LUCENE_PRIMARY_KEY_FIELD);
        return searcher.storedFields().document(docId, fieldsToLoad);
    }

    private SearchHit mapHit(
            Document document,
            ScoreDoc scoreDoc,
            Map<String, FieldDefinition> schema,
            SearchRequest request,
            Highlighter highlighter,
            SearchExecutor executor,
            Set<String> returnFieldSet
    ) {
        String documentId = document.get(LuceneUtils.LUCENE_PRIMARY_KEY_FIELD);

        Map<String, Object> fields = new LinkedHashMap<>();
        Set<String> seenFields = new HashSet<>();

        for (IndexableField indexableField : document.getFields()) {
            String fieldName = indexableField.name();

            if (LuceneUtils.LUCENE_PRIMARY_KEY_FIELD.equals(fieldName)) continue;


            if (returnFieldSet != null && !returnFieldSet.contains(fieldName)) continue;

            if (seenFields.contains(fieldName)) continue;
            seenFields.add(fieldName);

            FieldDefinition def = schema.get(fieldName);
            fields.put(fieldName, readFieldValue(document, fieldName, def));
        }

        Map<String, String> highlights = null;
        if (request.isHighlight() && highlighter != null) {
            highlights = extractHighlights(document, schema, highlighter, executor, returnFieldSet);
        }

        return SearchHit.builder()
                .documentId(documentId)
                .score(scoreDoc.score)
                .fields(fields)
                .highlights(highlights)
                .build();
    }


    private Object readFieldValue(Document document, String fieldName, FieldDefinition def) {
        if (def == null) {
            return document.get(fieldName);
        }

        switch (def.getType()) {
            case STRING:
            case BOOLEAN:
                return document.get(fieldName);

            case INTEGER: {
                IndexableField f = document.getField(fieldName);
                if (f != null && f.numericValue() != null) {
                    return f.numericValue().intValue();
                }
                return tryParseInt(document.get(fieldName), fieldName);
            }

            case LONG: {
                IndexableField f = document.getField(fieldName);
                if (f != null && f.numericValue() != null) {
                    return f.numericValue().longValue();
                }
                return tryParseLong(document.get(fieldName), fieldName);
            }

            case FLOAT: {
                IndexableField f = document.getField(fieldName);
                if (f != null && f.numericValue() != null) {
                    return f.numericValue().floatValue();
                }
                return tryParseFloat(document.get(fieldName), fieldName);
            }

            case DOUBLE: {
                IndexableField f = document.getField(fieldName);
                if (f != null && f.numericValue() != null) {
                    return f.numericValue().doubleValue();
                }
                return tryParseDouble(document.get(fieldName), fieldName);
            }

            default:
                return document.get(fieldName);
        }
    }

    /**
     * Extract highlight snippets for all STRING+analyzed+stored fields in this document.
     *
     * Only STRING fields with analyzed=true can be highlighted —
     * numeric and non-analyzed fields cannot be re-analyzed for highlighting.
     */
    private Map<String, String> extractHighlights(
            Document document,
            Map<String, FieldDefinition> schema,
            Highlighter highlighter,
            SearchExecutor executor,
            Set<String> returnFieldSet
    ) {
        Map<String, String> highlights = new LinkedHashMap<>();

        for (Map.Entry<String, FieldDefinition> entry : schema.entrySet()) {
            String fieldName = entry.getKey();
            FieldDefinition def = entry.getValue();

            // Only highlight analyzed STRING fields
            if (def.getType() != LuceneUtils.FieldType.STRING) continue;
            if (!Boolean.TRUE.equals(def.getAnalyzed())) continue;

            if (returnFieldSet != null && !returnFieldSet.contains(fieldName)) continue;

            String fieldValue = document.get(fieldName);
            String snippet = executor.highlight(highlighter, fieldName, fieldValue);
            if (snippet != null) {
                highlights.put(fieldName, snippet);
            }
        }

        return highlights.isEmpty() ? null : highlights;
    }


    private Object tryParseInt(String value, String fieldName) {
        if (value == null) return null;
        try { return Integer.parseInt(value); }
        catch (NumberFormatException e) {
            log.warn("Cannot parse INTEGER value '{}' for field '{}'", value, fieldName);
            return value;
        }
    }

    private Object tryParseLong(String value, String fieldName) {
        if (value == null) return null;
        try { return Long.parseLong(value); }
        catch (NumberFormatException e) {
            log.warn("Cannot parse LONG value '{}' for field '{}'", value, fieldName);
            return value;
        }
    }

    private Object tryParseFloat(String value, String fieldName) {
        if (value == null) return null;
        try { return Float.parseFloat(value); }
        catch (NumberFormatException e) {
            log.warn("Cannot parse FLOAT value '{}' for field '{}'", value, fieldName);
            return value;
        }
    }

    private Object tryParseDouble(String value, String fieldName) {
        if (value == null) return null;
        try { return Double.parseDouble(value); }
        catch (NumberFormatException e) {
            log.warn("Cannot parse DOUBLE value '{}' for field '{}'", value, fieldName);
            return value;
        }
    }
}