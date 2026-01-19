package com.rag.documentloader.controller;

import com.rag.documentloader.model.SearchQueryRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final VectorStore vectorStore;
    private final ChatClient.Builder chatClientBuilder;
    private static final double SIMILARITY_THRESHOLD = 0.5;

    public SearchController(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClientBuilder = chatClientBuilder;
    }

    @PostMapping("/query")
    public String search(@RequestBody SearchQueryRequest request) {

        ChatClient chatClient = chatClientBuilder.build();
        List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest.builder().query(request.getQuery())
                        .topK(5)
                        .build());
        List<Document> relevantDocs = similarDocs.stream()
                .filter(doc -> {
                    Object distanceObj = doc.getMetadata().get("distance");
                    if (distanceObj == null)
                        return false;
                    var similarity = 0f;
                    if (distanceObj instanceof Float) {
                        similarity = (Float) distanceObj;
                    } else if (distanceObj instanceof Double) {
                        similarity = ((Double) distanceObj).floatValue();
                    }
                    return similarity >= SIMILARITY_THRESHOLD;
                }).toList();

        // Check if we found relevant documents
        if (relevantDocs.isEmpty()) {
            return "No relevant documents found for your query: " + request.getQuery();
        }

        // Extract text from relevant documents
        String context = relevantDocs.stream()
                .map(Document::getText)
                .reduce((a, b) -> a + "\n\n" + b)
                .orElse("No documents found");

       return chatClient.prompt()
                .user("Based on this context:\n" + context + "\n\nAnswer this question: " + request.getQuery())
                .call()
                .content();

    }
}