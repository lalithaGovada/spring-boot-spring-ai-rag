package com.example.airag.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

// Sends the message straight to the model. Does not search the policy files.
@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(@Qualifier("plainChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public ChatReply ask(String message) {
        if (!StringUtils.hasText(message)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message is required");
        }
        // prompt -> user text -> call the model -> take the text answer.
        String answer = chatClient.prompt().user(message).call().content();
        return new ChatReply(answer);
    }
}
