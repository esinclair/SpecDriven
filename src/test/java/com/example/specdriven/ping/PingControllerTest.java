package com.example.specdriven.ping;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PingController.class)
@Import(com.example.specdriven.error.GlobalExceptionHandler.class)
class PingControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void ping_returnsPongWithinBudget() throws Exception {
        long start = System.nanoTime();

        mvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("pong"));

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        // Loose 1s budget assertion (unit/integration environment variance)
        org.junit.jupiter.api.Assertions.assertTrue(elapsedMs < 1000, "Expected < 1000ms but was " + elapsedMs + "ms");
    }
}
