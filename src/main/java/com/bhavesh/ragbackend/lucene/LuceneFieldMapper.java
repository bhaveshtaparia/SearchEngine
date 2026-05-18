package com.bhavesh.ragbackend.lucene;

import com.bhavesh.ragbackend.dto.DynamicField;
import com.bhavesh.ragbackend.lucene.fieldHandler.FieldHandler;
import org.apache.lucene.index.IndexableField;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class LuceneFieldMapper {

    private final List<FieldHandler> handlers;

    public LuceneFieldMapper(List<FieldHandler> handlers) {
        this.handlers = handlers;
    }

    public List<IndexableField> mapField(String fieldName, DynamicField dynamicField) {
        String type = dynamicField.getType().toLowerCase();

        return handlers.stream()
                .filter(h -> h.supports(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported field type: " + type))
                .createFields(fieldName, dynamicField);
    }
}