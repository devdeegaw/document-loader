package com.rag.documentloader.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    public String search(@RequestBody QueryRequest request) { // ✅ Returns plain String
        // Build ChatClient
        try {
            ChatClient chatClient = chatClientBuilder.build();

            // Search similar documents with similarity scores
            List<Document> similarDocs = vectorStore.similaritySearch(
                    SearchRequest.builder().query(request.getQuery())
                            .topK(5)
                            .build());

            // ✅ Filter by similarity threshold
            List<Document> relevantDocs = similarDocs.stream()
                    .filter(doc -> {
                        Object distanceObj = doc.getMetadata().get("distance");
                        if (distanceObj == null)
                            return false;

                        // ✅ Handle both Float and Double
                        float similarity = 0f;
                        if (distanceObj instanceof Float) {
                            similarity = (Float) distanceObj;
                        } else if (distanceObj instanceof Double) {
                            similarity = ((Double) distanceObj).floatValue();
                        }

                        return similarity >= SIMILARITY_THRESHOLD;
                    })
                    .collect(Collectors.toList());

            // Check if we found relevant documents
            if (relevantDocs.isEmpty()) {
                return "तुमच्या प्रश्नासाठी संबंधित दस्तऐवज सापडले नाहीत: " + request.getQuery();
            }

            // Extract text from relevant documents
            String context = relevantDocs.stream()
                    .map(doc -> doc.getText())
                    .reduce((a, b) -> a + "\n\n" + b)
                    .orElse("No documents found");

            // Generate response using LLM with context - MARATHI OUTPUT
            String response = chatClient.prompt()
                    .system("तुम्ही मराठी भाषेतील सहाय्यक आहात. तुम्हाला दिलेल्या संदर्भावर आधारित प्रश्नांची उत्तरे देवनागरी लिपीत मराठीमध्ये द्यावीत.")
                    .user("संदर्भ:\n" + context + "\n\nप्रश्न: " + request.getQuery())
                    .call()
                    .content();

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return "त्रुटी: " + e.getMessage();
        }

    }

    public static class QueryRequest {
        private String query;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }
}