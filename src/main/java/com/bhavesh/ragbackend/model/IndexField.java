package com.bhavesh.ragbackend.model;


import com.bhavesh.ragbackend.dto.FieldDefinition;
import com.bhavesh.ragbackend.utils.LuceneUtils;
import lombok.Data;

@Data
public class IndexField {

    private Object value;

    private String fieldName;

    /*
        string
        integer
        long
        float
        double
        boolean
    */
    private LuceneUtils.FieldType type;

    /*
        default true
    */
    private Boolean searchable = true;

    /*
        default true
    */
    private Boolean stored = true;

    /*
        default false
    */
    private Boolean sortable = false;

    /*
        only for string
        default true
    */
    private Boolean analyzed = true;

    public IndexField (String fieldName, Object value, LuceneUtils.FieldType type) {
        this.fieldName = fieldName;
        this.value = value;
        this.type = type;
    }

    public void consumeFieldDefinition(FieldDefinition fieldDefinition) {
        this.searchable = fieldDefinition.getSearchable();
        this.stored = fieldDefinition.getStored();
        this.sortable =  fieldDefinition.getSortable();
        this.analyzed =  fieldDefinition.getAnalyzed();
    }
}