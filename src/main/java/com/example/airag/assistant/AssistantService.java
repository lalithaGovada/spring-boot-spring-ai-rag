package com.example.airag.assistant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

// Asks the model a question. The model can call OfficeTools before it replies.
@Service
public class AssistantService {

    private final ChatClient chatClient;

    public AssistantService(@Qualifier("toolChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public AssistantAnswer ask(String question) {
        if (!StringUtils.hasText(question)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question is required");
        }
        String answer = chatClient.prompt().user(question).call().content();
        return new AssistantAnswer(answer);
    }
}
