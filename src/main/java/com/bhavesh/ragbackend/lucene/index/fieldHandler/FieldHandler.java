package com.bhavesh.ragbackend.lucene.index.fieldHandler;

import com.bhavesh.ragbackend.model.IndexField;
import com.bhavesh.ragbackend.utils.LuceneUtils;
import org.apache.lucene.index.IndexableField;

import java.util.List;

public interface FieldHandler {

    boolean supports(LuceneUtils.FieldType type);

    List<IndexableField> createFields(
            IndexField indexField
    );
}
