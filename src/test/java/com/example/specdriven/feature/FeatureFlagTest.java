package com.example.specdriven.feature;

import com.example.specdriven.SpecDrivenApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for feature flag functionality.
 * Tests T043-T045: Feature flag interceptor behavior.
 */
@SpringBootTest(classes = SpecDrivenApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "feature-flag.users-api=false"
})
class FeatureFlagTest {

    @Autowired
    MockMvc mvc;

    @Test
    void whenFeatureFlagDisabled_usersEndpointReturns404() throws Exception {
        // T043: Test users API with flag disabled (expect 404)
        mvc.perform(get("/users")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FEATURE_DISABLED"));
    }

    @Test
    void whenFeatureFlagDisabled_loginEndpointReturns404() throws Exception {
        // T043: Test login endpoint with flag disabled (expect 404)
        mvc.perform(post("/login")
                        .contentType("application/json")
                        .content("{\"username\":\"test\",\"password\":\"test\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FEATURE_DISABLED"));
    }

    @Test
    void whenFeatureFlagDisabled_errorResponseIncludesFeatureDisabledCode() throws Exception {
        // T045: Verify error response includes FEATURE_DISABLED code when flag is off
        mvc.perform(get("/users")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FEATURE_DISABLED"))
                .andExpect(jsonPath("$.message").value("Feature not available"));
    }

    @Test
    void whenFeatureFlagDisabled_pingEndpointStillWorks() throws Exception {
        // Verify ping endpoint is not affected by feature flags
        mvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("pong"));
    }
}
