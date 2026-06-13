package com.bhavesh.ragbackend.lucene.index.fieldHandler;

import com.bhavesh.ragbackend.model.IndexField;
import com.bhavesh.ragbackend.utils.LuceneUtils;
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

    private static final Set<LuceneUtils.FieldType> SUPPORTED = Set.of(LuceneUtils.FieldType.STRING, LuceneUtils.FieldType.BOOLEAN);

    @Override
    public boolean supports(LuceneUtils.FieldType type) {
        return SUPPORTED.contains(type);
    }

    @Override
    public List<IndexableField> createFields(IndexField indexField) {

        String fieldName = indexField.getFieldName();

        Object rawValue = indexField.getValue();

        boolean searchable = indexField.getSearchable();

        boolean analyzed = indexField.getAnalyzed();

        validateValue(fieldName, rawValue);

        String value =  rawValue.toString();

        List<IndexableField> fields = new ArrayList<>();

        Field.Store store =Field.Store.YES;

        if (searchable) {

            if (analyzed) {

                // TextField tokenizes the value — use for full-text search.
                fields.add(new TextField(fieldName, value, store));

            } else {

                // StringField indexes the value as a single token — use for
                // exact-match queries (e.g. IDs, tags, enum values).
                fields.add(new StringField(fieldName, value, store));
            }

        } else {

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

        if (!(value instanceof String || value instanceof Boolean)) {
            throw new IllegalArgumentException("Field '" + fieldName + "' expected a String, " + "but got: " + value.getClass().getSimpleName() + " (value: " + value + ").");
        }
    }
}