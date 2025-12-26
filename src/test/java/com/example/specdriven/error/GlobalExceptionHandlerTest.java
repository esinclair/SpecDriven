package com.example.specdriven.error;

import com.example.specdriven.ping.PingController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PingController.class, GlobalExceptionHandlerTest.ThrowingController.class})
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mvc;

    @RestController
    static class ThrowingController {
        @GetMapping("/v1/boom")
        public String boom() {
            throw new RuntimeException("boom");
        }
    }

    @Test
    void unhandledException_returnsStandardErrorShape() throws Exception {
        mvc.perform(get("/v1/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Unexpected server error"))
                .andExpect(jsonPath("$.details.path").value("/v1/boom"));
    }
}
