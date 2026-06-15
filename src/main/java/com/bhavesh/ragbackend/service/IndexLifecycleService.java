package com.bhavesh.ragbackend.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class IndexLifecycleService {

    private final IndexingService indexingService;
    private final SchemaService schemaService;

    public void deleteFolder(String folderId){
        List<String> indexes = schemaService.getIndexes(folderId);
          for(String indexId:indexes){
              deleteIndex(folderId,indexId);
          }
          schemaService.deleteFolder(folderId);
    }


    public void deleteIndex(String folderId, String indexId){
        indexingService.deleteIndex(folderId,indexId);
        schemaService.deleteIndex(folderId,indexId);
    }


}
