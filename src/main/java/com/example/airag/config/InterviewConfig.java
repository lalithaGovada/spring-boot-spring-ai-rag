package com.example.airag.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.airag.assistant.OfficeTools;

// One ChatClient per interview topic, so the FAQ search does not run on the other endpoints.
@Configuration
public class InterviewConfig {

    // POST /api/chat. No documents, no memory, no tools. The model answers from general knowledge.
    @Bean
    ChatClient plainChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("You answer in one or two short sentences.")
                .build();
    }

    // POST /api/tickets. The prompt tells the model which fields to fill. TicketService maps the reply onto a Java record.
    @Bean
    ChatClient structuredChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        You turn a support note into a ticket.
                        Category is one of: leave, expense, remote-work, other.
                        Priority is one of: low, medium, high.
                        Summary is one sentence.
                        """)
                .build();
    }

    // Keeps the last 10 messages for each conversationId. Lost when the app stops.
    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().maxMessages(10).build();
    }

    // POST /api/conversations. The advisor pastes earlier messages from this conversation into the next prompt.
    @Bean
    ChatClient memoryChatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultSystem("You remember this conversation. Answer in one or two short sentences.")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    // POST /api/assistant. defaultTools lets the model call methods in OfficeTools.
    @Bean
    ChatClient toolChatClient(ChatClient.Builder builder, OfficeTools officeTools) {
        return builder
                .defaultSystem("""
                        You look up office facts by calling the available tool.
                        If the tool has no record, say so.
                        Do not invent a desk, extension, or team.
                        """)
                .defaultTools(officeTools)
                .build();
    }
}
