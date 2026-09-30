package com.example.airag.ticket;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

// Turns one support note into a TicketSummary object.
@Service
public class TicketService {

    private final ChatClient chatClient;

    public TicketService(@Qualifier("structuredChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public TicketSummary summarize(String note) {
        if (!StringUtils.hasText(note)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Note is required");
        }
        // entity(...) asks the model for JSON and maps it onto TicketSummary.
        return chatClient.prompt()
                .user(note)
                .call()
                .entity(TicketSummary.class);
    }
}
