package com.example.airag.conversation;

// Body of POST /api/conversations. Reuse conversationId to continue the same chat.
public record ConversationTurn(String conversationId, String message) {
}
