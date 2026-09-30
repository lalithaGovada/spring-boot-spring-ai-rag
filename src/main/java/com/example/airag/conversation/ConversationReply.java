package com.example.airag.conversation;

// Reply plus the conversationId, so you can see which chat this answer belongs to.
public record ConversationReply(String conversationId, String answer) {
}
