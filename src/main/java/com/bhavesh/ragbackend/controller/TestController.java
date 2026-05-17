package com.bhavesh.ragbackend.controller;

import com.bhavesh.ragbackend.lucene.IndexWriterManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final IndexWriterManager writerManager;

    public TestController(
            IndexWriterManager writerManager
    ) {
        this.writerManager = writerManager;
    }

    @GetMapping("/test")
    public String test() throws Exception {

        writerManager.getWriter(
                "finance",
                "invoices"
        );

        return "Writer Created";
    }
}