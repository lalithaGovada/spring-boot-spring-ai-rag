package com.example.airag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Starts the app. Five endpoints live under /api: chat, faq, tickets, conversations, assistant.
@SpringBootApplication
public class AiRagApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiRagApplication.class, args);
    }
}
