package com.bhavesh.ragbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchResponse {

    private List<SearchHit> hits;

}