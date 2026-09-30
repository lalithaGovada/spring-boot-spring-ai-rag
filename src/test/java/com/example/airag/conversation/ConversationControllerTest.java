package com.example.airag.conversation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ConversationController.class)
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConversationService conversationService;

    @Test
    void keepsTheConversationId() throws Exception {
        when(conversationService.send("lalitha-1", "My name is Lalitha"))
                .thenReturn(new ConversationReply("lalitha-1", "Hello Lalitha."));

        mockMvc.perform(post("/api/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conversationId\":\"lalitha-1\",\"message\":\"My name is Lalitha\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value("lalitha-1"))
                .andExpect(jsonPath("$.answer").value("Hello Lalitha."));
    }
}
