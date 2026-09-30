package com.example.airag.faq;

import java.util.List;

// FAQ reply: the model text, plus the policy file names that were searched.
public record Answer(String answer, List<String> sources) {
}
