package com.example.airag.ticket;

// The three fields the model must fill. category: leave, expense, remote-work, or other.
public record TicketSummary(String category, String priority, String summary) {
}
