package com.bhavesh.ragbackend.dto;

import com.bhavesh.ragbackend.utils.LuceneUtils;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FieldDefinition {

    @NotNull(message = "Field type must not be null")
    private LuceneUtils.FieldType type;


    private Boolean primaryKey = false;

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

    public boolean isPrimaryKey() {
        return  primaryKey;
    }
}
