package com.bhavesh.ragbackend.lucene.fieldHandler;

import com.bhavesh.ragbackend.dto.DynamicField;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class StringFieldHandler implements FieldHandler {

    private static final Logger log = LoggerFactory.getLogger(StringFieldHandler.class);

    private static final Set<String> SUPPORTED = Set.of("string", "boolean");

    @Override
    public boolean supports(String type) {
        return SUPPORTED.contains(type);
    }

    @Override
    public List<IndexableField> createFields(String fieldName, DynamicField dynamicField) {

        Object rawValue = dynamicField.getValue();

        boolean searchable = dynamicField.getSearchable();

        boolean stored = dynamicField.getStored();

        boolean analyzed = dynamicField.getAnalyzed();

        validateValue(fieldName, rawValue);
        warnIfNoOp(fieldName, searchable, stored);

        String value = (String) rawValue;

        List<IndexableField> fields = new ArrayList<>();

        Field.Store store = stored ? Field.Store.YES : Field.Store.NO;

        if (searchable) {

            if (analyzed) {

                // TextField tokenizes the value — use for full-text search.
                fields.add(new TextField(fieldName, value, store));

            } else {

                // StringField indexes the value as a single token — use for
                // exact-match queries (e.g. IDs, tags, enum values).
                fields.add(new StringField(fieldName, value, store));
            }

        } else if (stored) {

            // Not searchable but stored — value can be retrieved,
            // but will not appear in search results on its own.
            fields.add(new StoredField(fieldName, value));
        }

        return fields;
    }

    /**
     * Validates that the field value is non-null and a String,
     * giving a clear message before any cast is attempted.
     */
    private void validateValue(String fieldName, Object value) {

        if (value == null) {
            throw new IllegalArgumentException("Field '" + fieldName + "' has a null value. " + "String fields must have a non-null value.");
        }

        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Field '" + fieldName + "' expected a String, " + "but got: " + value.getClass().getSimpleName() + " (value: " + value + ").");
        }
    }

    /**
     * Warns when both flags are false, which would silently produce
     * an empty field list and drop the value from the index entirely.
     */
    private void warnIfNoOp(String fieldName, boolean searchable, boolean stored) {

        if (!searchable && !stored) {
            log.warn("Field '{}' has searchable=false and stored=false. " + "No Lucene fields will be created — this value will not be indexed.", fieldName);
        }
    }
}