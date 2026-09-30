package com.example.airag.conversation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

// Sends a message and attaches it to one conversationId so later questions can see it.
@Service
public class ConversationService {

    private final ChatClient chatClient;

    public ConversationService(@Qualifier("memoryChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public ConversationReply send(String conversationId, String message) {
        if (!StringUtils.hasText(conversationId) || !StringUtils.hasText(message)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation id and message are required");
        }
        // The conversationId picks which saved messages to include. A new id starts a fresh chat.
        String answer = chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(message)
                .call()
                .content();
        return new ConversationReply(conversationId, answer);
    }
}
