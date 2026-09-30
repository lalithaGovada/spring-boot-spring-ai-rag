package com.example.airag.ticket;

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

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @Test
    void returnsATypedTicket() throws Exception {
        when(ticketService.summarize("I need 3 extra vacation days next week"))
                .thenReturn(new TicketSummary("leave", "medium", "Employee wants 3 extra vacation days next week."));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"I need 3 extra vacation days next week\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("leave"))
                .andExpect(jsonPath("$.priority").value("medium"));
    }
}
