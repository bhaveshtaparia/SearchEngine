package com.bhavesh.ragbackend.lucene.search;

import com.bhavesh.ragbackend.dto.schema.FieldDefinition;
import com.bhavesh.ragbackend.dto.search.SearchRequest;
import com.bhavesh.ragbackend.exception.LuceneSearchException;
import com.bhavesh.ragbackend.utils.LuceneUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class QueryBuilder {

    private static final Logger log = LoggerFactory.getLogger(QueryBuilder.class);

    private final Analyzer analyzer;

    public QueryBuilder(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    /**
     * Result structure:
     *   BooleanQuery
     *     MUST   → MultiFieldQueryParser result (full-text across all text fields)
     *     FILTER → TermQuery / PointRangeQuery per filter entry (exact matches)
     */
    public Query build(SearchRequest request, Map<String, FieldDefinition> schema) {
        try {
            BooleanQuery.Builder boolQuery = new BooleanQuery.Builder();

            // ── 1. Full-text MUST clause ─────────────────────────────────────
            Query textQuery = buildTextQuery(request, schema);
            boolQuery.add(textQuery, BooleanClause.Occur.MUST);

            // ── 2. Exact FILTER clauses ──────────────────────────────────────
            if (request.getFilters() != null && !request.getFilters().isEmpty()) {
                List<Query> filterQueries = buildFilterQueries(request.getFilters(), schema);
                for (Query filterQuery : filterQueries) {
                    boolQuery.add(filterQuery, BooleanClause.Occur.FILTER);
                }
            }

            return boolQuery.build();

        } catch (ParseException e) {
            // QueryParser throws ParseException for invalid syntax like "AND AND" or unclosed quotes
            log.warn("Query parse failed for query='{}': {}", request.getQuery(), e.getMessage());
            throw new LuceneSearchException("Invalid query");
        }
    }


    private Query buildTextQuery(SearchRequest request,
                                 Map<String, FieldDefinition> schema) throws ParseException {

        List<String> resolvedFields = resolveSearchFields(request, schema);

        if (resolvedFields == null || resolvedFields.isEmpty()) {
            throw new LuceneSearchException(
                    "No searchable STRING fields found in schema. " +
                            "At least one field must have type=STRING and searchable=true."
            );
        }

        Map<String, Float> boosts = new HashMap<>();
        for (String field : resolvedFields) {
            boosts.put(field, 1.0f);
        }

        String[] fieldArray = resolvedFields.toArray(new String[0]);
        MultiFieldQueryParser parser = new MultiFieldQueryParser(fieldArray, analyzer, boosts);
        parser.setDefaultOperator(QueryParser.Operator.AND);
        String escapedQuery = QueryParser.escape(request.getQuery());
        return parser.parse(escapedQuery);
    }


    private List<String> resolveSearchFields(SearchRequest request,
                                             Map<String, FieldDefinition> schema) {
        if (request.getSearchFields() != null && !request.getSearchFields().isEmpty()) {
            // Explicit field list — validate each
            for (String fieldName : request.getSearchFields()) {
                FieldDefinition def = schema.get(fieldName);
                if (def == null) {
                    throw new LuceneSearchException(
                            "searchFields contains unknown field: '" + fieldName + "'"
                    );
                }
                if (def.getType() != LuceneUtils.FieldType.STRING) {
                    throw new LuceneSearchException(
                            "Field '" + fieldName + "' has type " + def.getType() +
                                    " — only STRING fields support full-text search. " +
                                    "Use filters for exact numeric / boolean matching."
                    );
                }
                if (!Boolean.TRUE.equals(def.getSearchable())) {
                    throw new LuceneSearchException(
                            "Field '" + fieldName + "' has searchable=false — cannot search it."
                    );
                }
                if (!Boolean.TRUE.equals(def.getAnalyzed())) {
                    throw new LuceneSearchException(
                            "Field '" + fieldName + "' has analyzed=false. " +
                                    "It stores raw values and supports only exact-match filters, not full-text search."
                    );
                }
            }
            return request.getSearchFields();
        }

        List<String> fields = new ArrayList<>();
        for (Map.Entry<String, FieldDefinition> entry : schema.entrySet()) {
            FieldDefinition def = entry.getValue();
            if (def.getType() == LuceneUtils.FieldType.STRING
                    && Boolean.TRUE.equals(def.getSearchable())
                    && Boolean.TRUE.equals(def.getAnalyzed())) {
                fields.add(entry.getKey());
            }
        }
        return fields;
    }


    private List<Query> buildFilterQueries(Map<String, String> filters,
                                           Map<String, FieldDefinition> schema) {
        List<Query> queries = new ArrayList<>();

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String fieldName = entry.getKey();
            String rawValue  = entry.getValue();

            FieldDefinition def = schema.get(fieldName);
            if (def == null) {
                throw new LuceneSearchException(
                        "filters contains unknown field: '" + fieldName + "'"
                );
            }

            queries.add(buildSingleFilter(fieldName, rawValue, def));
        }

        return queries;
    }

    private Query buildSingleFilter(String fieldName, String rawValue, FieldDefinition def) {
        switch (def.getType()) {

            case STRING: {
                if (Boolean.TRUE.equals(def.getAnalyzed())) {
                    throw new LuceneSearchException(
                            "Field '" + fieldName + "' is a full-text (analyzed) STRING field. " +
                                    "You cannot use it as an exact filter. " +
                                    "Either set analyzed=false in the schema for keyword-style matching, " +
                                    "or use searchFields to full-text search it instead."
                    );
                }
                // Non-analyzed STRING → raw value stored as-is → exact TermQuery
                return new TermQuery(new Term(fieldName, rawValue));
            }

            case BOOLEAN: {
                // Normalize to "true"/"false" regardless of input casing
                String normalized = Boolean.parseBoolean(rawValue) ? "true" : "false";
                return new TermQuery(new Term(fieldName, normalized));
            }

            case INTEGER: {
                try {
                    int value = Integer.parseInt(rawValue);
                    return IntPoint.newRangeQuery(fieldName, value, value);
                } catch (NumberFormatException e) {
                    throw new LuceneSearchException(
                            "Filter value '" + rawValue + "' for INTEGER field '" + fieldName + "' is not a valid integer."
                    );
                }
            }

            case LONG: {
                try {
                    long value = Long.parseLong(rawValue);
                    return LongPoint.newRangeQuery(fieldName, value, value);
                } catch (NumberFormatException e) {
                    throw new LuceneSearchException(
                            "Filter value '" + rawValue + "' for LONG field '" + fieldName + "' is not a valid long."
                    );
                }
            }

            case FLOAT: {
                try {
                    float value = Float.parseFloat(rawValue);
                    return FloatPoint.newRangeQuery(fieldName, value, value);
                } catch (NumberFormatException e) {
                    throw new LuceneSearchException(
                            "Filter value '" + rawValue + "' for FLOAT field '" + fieldName + "' is not a valid float."
                    );
                }
            }

            case DOUBLE: {
                try {
                    double value = Double.parseDouble(rawValue);
                    return DoublePoint.newRangeQuery(fieldName, value, value);
                } catch (NumberFormatException e) {
                    throw new LuceneSearchException(
                            "Filter value '" + rawValue + "' for DOUBLE field '" + fieldName + "' is not a valid double."
                    );
                }
            }

            default:
                throw new LuceneSearchException(
                        "Unsupported field type for filtering: " + def.getType()
                );
        }
    }
}