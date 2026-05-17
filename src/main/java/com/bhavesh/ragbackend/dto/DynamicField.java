package com.bhavesh.ragbackend.dto;


import lombok.Data;

@Data
public class DynamicField {

    private Object value;

    /*
        string
        integer
        long
        float
        double
        boolean
    */
    private String type;

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
}