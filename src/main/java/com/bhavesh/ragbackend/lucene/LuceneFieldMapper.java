package com.bhavesh.ragbackend.lucene;

import com.bhavesh.ragbackend.lucene.exception.LuceneIndexException;
import com.bhavesh.ragbackend.model.IndexField;
import com.bhavesh.ragbackend.lucene.fieldHandler.FieldHandler;
import com.bhavesh.ragbackend.utils.LuceneUtils;
import org.apache.lucene.index.IndexableField;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class LuceneFieldMapper {

    private final List<FieldHandler> handlers;

    public LuceneFieldMapper(List<FieldHandler> handlers) {
        this.handlers = handlers;
    }

    public List<IndexableField> mapField(IndexField indexField) {
        LuceneUtils.FieldType type = indexField.getType();

        return handlers.stream()
                .filter(h -> h.supports(type))
                .findFirst()
                .orElseThrow(() -> new LuceneIndexException("Unsupported field type: " + type))
                .createFields(indexField);
    }
}