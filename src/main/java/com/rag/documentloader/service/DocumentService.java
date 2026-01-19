package com.rag.documentloader.service;

import com.rag.documentloader.model.SearchQueryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Value("classpath:/templates/documentTemplate.st")
    private Resource documentTemplate;

    public String askQuestion(SearchQueryRequest request) {

        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(request.getQuery()).similarityThreshold(0.7).build());

        if (docs.isEmpty()) {
            // Option A: Just ask the LLM directly without context
            return chatClient.prompt()
                    .user("I didn't find this in my docs. " + request.getQuery())
                    .call().content();
        }

        return chatClient.prompt()
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .promptTemplate(PromptTemplate.builder().resource(documentTemplate).build()).build())
                .user(request.getQuery())
                .call().content();
    }
}
