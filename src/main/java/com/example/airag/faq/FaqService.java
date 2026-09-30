package com.example.airag.faq;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

// Asks the RAG ChatClient, then reads which policy files were used.
@Service
public class FaqService {

    private final ChatClient chatClient;

    public FaqService(@Qualifier("chatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public Answer ask(String question) {
        if (!StringUtils.hasText(question)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question is required");
        }

        // QuestionAnswerAdvisor searches the vector store before this call reaches the model.
        ChatResponse response = chatClient.prompt()
                .user(question)
                .call()
                .chatResponse();

        return new Answer(response.getResult().getOutput().getText(), sources(response));
    }

    // The advisor stores the matched chunks on the response. We return their file names.
    private List<String> sources(ChatResponse response) {
        Object retrieved = response.getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
        if (!(retrieved instanceof List<?> documents)) {
            return List.of();
        }
        return documents.stream()
                .filter(Document.class::isInstance)
                .map(Document.class::cast)
                .map(document -> String.valueOf(document.getMetadata().getOrDefault("source", "unknown")))
                .distinct()
                .toList();
    }
}
