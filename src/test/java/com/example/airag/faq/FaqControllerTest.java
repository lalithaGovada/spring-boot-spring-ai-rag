package com.example.airag.faq;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(FaqController.class)
class FaqControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FaqService faqService;

    @Test
    void answersFromCompanyPolicy() throws Exception {
        when(faqService.ask("How many vacation days do I get?"))
                .thenReturn(new Answer("18 paid vacation days each calendar year.", List.of("leave-policy.txt")));

        mockMvc.perform(post("/api/faq")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"How many vacation days do I get?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("18 paid vacation days each calendar year."))
                .andExpect(jsonPath("$.sources[0]").value("leave-policy.txt"));
    }

    @Test
    void rejectsABlankQuestion() throws Exception {
        when(faqService.ask(" "))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question is required"));

        mockMvc.perform(post("/api/faq")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\" \"}"))
                .andExpect(status().isBadRequest());
    }
}
