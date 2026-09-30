package com.example.airag.assistant;

import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

// A normal Java method the model is allowed to call. Only Lalitha and Kalyani are on file.
@Component
public class OfficeTools {

    private final Map<String, String> desks = Map.of(
            "lalitha", "Building A, desk 12, extension 4401, platform team",
            "kalyani", "Building B, desk 4, extension 4408, finance team");

    // The description is what the model reads when it decides whether to call this method.
    @Tool(description = "Look up an employee's desk, phone extension, and team by first name")
    public String deskFor(@ToolParam(description = "Employee first name") String name) {
        if (name == null) {
            return "No record";
        }
        return desks.getOrDefault(name.trim().toLowerCase(), "No record for " + name);
    }
}
