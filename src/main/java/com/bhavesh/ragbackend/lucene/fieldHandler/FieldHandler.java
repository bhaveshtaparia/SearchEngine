package com.bhavesh.ragbackend.lucene.fieldHandler;

import com.bhavesh.ragbackend.dto.DynamicField;
import org.apache.lucene.index.IndexableField;

import java.util.List;

public interface FieldHandler {

    List<IndexableField> createFields(
            String fieldName,
            DynamicField dynamicField
    );
}
