package com.example.airag.assistant;

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

@WebMvcTest(AssistantController.class)
class AssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssistantService assistantService;

    @Test
    void returnsTheToolAnswer() throws Exception {
        when(assistantService.ask("Where does Lalitha sit?"))
                .thenReturn(new AssistantAnswer("Building A, desk 12."));

        mockMvc.perform(post("/api/assistant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Where does Lalitha sit?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Building A, desk 12."));
    }
}
