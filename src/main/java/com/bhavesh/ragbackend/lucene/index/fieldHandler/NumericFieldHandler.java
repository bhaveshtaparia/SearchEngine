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
import java.util.function.Function;

@Component
public class NumericFieldHandler implements FieldHandler {

    private static final Logger log = LoggerFactory.getLogger(NumericFieldHandler.class);
    private static final Set<LuceneUtils.FieldType> SUPPORTED = Set.of(LuceneUtils.FieldType.INTEGER, LuceneUtils.FieldType.LONG, LuceneUtils.FieldType.FLOAT, LuceneUtils.FieldType.DOUBLE);

    @Override
    public boolean supports(LuceneUtils.FieldType type) {
        return SUPPORTED.contains(type);
    }
    @Override
    public List<IndexableField> createFields(IndexField indexField) {

        LuceneUtils.FieldType type = indexField.getType();

        Object value = indexField.getValue();

        String fieldName = indexField.getFieldName();

        boolean searchable = indexField.getSearchable();

        boolean stored = indexField.getStored();

        boolean sortable = indexField.getSortable();

        validateValue(fieldName, value);
        warnIfNoOp(fieldName, searchable, stored, sortable);

        List<IndexableField> fields = new ArrayList<>();

        switch (type) {

            case LuceneUtils.FieldType.INTEGER:

                int intValue = ((Number) value).intValue();

                addNumericFields(fields, intValue, searchable, stored, sortable, v -> new IntPoint(fieldName, v), v -> new StoredField(fieldName, v), v -> new NumericDocValuesField(fieldName, v.longValue()));

                break;

            case LuceneUtils.FieldType.LONG:

                long longValue = ((Number) value).longValue();

                addNumericFields(fields, longValue, searchable, stored, sortable, v -> new LongPoint(fieldName, v), v -> new StoredField(fieldName, v), v -> new NumericDocValuesField(fieldName, v));

                break;

            case LuceneUtils.FieldType.FLOAT:

                float floatValue = ((Number) value).floatValue();

                // NOTE: downstream sort queries must use SortField.Type.FLOAT
                // when sorting on a field backed by FloatDocValuesField.
                addNumericFields(fields, floatValue, searchable, stored, sortable, v -> new FloatPoint(fieldName, v), v -> new StoredField(fieldName, v), v -> new FloatDocValuesField(fieldName, v));

                break;

            case LuceneUtils.FieldType.DOUBLE:

                double doubleValue = ((Number) value).doubleValue();

                addNumericFields(fields, doubleValue, searchable, stored, sortable, v -> new DoublePoint(fieldName, v), v -> new StoredField(fieldName, v), v -> new DoubleDocValuesField(fieldName, v));

                break;

            default:

                throw new IllegalArgumentException("Unsupported numeric type '" + type + "' for field '" + fieldName + "'. Supported types: integer, long, float, double.");
        }

        return fields;
    }

    private <T extends Number> void addNumericFields(List<IndexableField> fields, T value, boolean searchable, boolean stored, boolean sortable, Function<T, IndexableField> pointFieldCreator, Function<T, IndexableField> storedFieldCreator, Function<T, IndexableField> docValueCreator) {

        if (searchable) {

            fields.add(pointFieldCreator.apply(value));
        }

        if (stored) {

            fields.add(storedFieldCreator.apply(value));
        }

        if (sortable) {

            fields.add(docValueCreator.apply(value));
        }
    }

    /**
     * Validates that the field value is non-null and a Number,
     * giving a clear message before any cast is attempted.
     */
    private void validateValue(String fieldName, Object value) {

        if (value == null) {
            throw new IllegalArgumentException("Field '" + fieldName + "' has a null value. " + "Numeric fields must have a non-null value.");
        }

        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("Field '" + fieldName + "' expected a Number, " + "but got: " + value.getClass().getSimpleName() + " (value: " + value + ").");
        }
    }

    /**
     * Warns when all indexing flags are false, which would silently
     * produce an empty field list and drop the value from the index.
     */
    private void warnIfNoOp(String fieldName, boolean searchable, boolean stored, boolean sortable) {

        if (!searchable && !stored && !sortable) {
            log.warn("Field '{}' has searchable=false, stored=false, and sortable=false. " + "No Lucene fields will be created — this value will not be indexed.", fieldName);
        }
    }
}