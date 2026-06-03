package com.bhavesh.ragbackend.lucene;

import com.bhavesh.ragbackend.model.IndexField;
import com.bhavesh.ragbackend.utils.LuceneUtils;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentBuilder {

    private static final Logger log = LoggerFactory.getLogger(DocumentBuilder.class);

    private final LuceneFieldMapper luceneFieldMapper;

    public Document build(String documentId, List<IndexField> indexFields) {


        Document document = new Document();

        /*
            Required system field
            Used for upsert/updateDocument()
        */
        document.add(new StringField(LuceneUtils.LUCENE_PRIMARY_KEY_FIELD, documentId, Field.Store.YES));

        for (IndexField indexField : indexFields) {

            List<IndexableField> luceneFields = luceneFieldMapper.mapField(indexField);

            luceneFields.forEach(document::add);
        }

        return document;
    }


}