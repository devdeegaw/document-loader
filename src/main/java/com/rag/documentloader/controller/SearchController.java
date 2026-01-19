package com.rag.documentloader.controller;

import com.rag.documentloader.model.SearchQueryRequest;
import com.rag.documentloader.service.DocumentService;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;

import javax.print.Doc;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@AllArgsConstructor
public class SearchController {

    private final DocumentService documentService;

    private static final double SIMILARITY_THRESHOLD = 0.5;


    @PostMapping("/query")
    public String search(@RequestBody SearchQueryRequest request) {
        return documentService.askQuestion(request);

    }
}