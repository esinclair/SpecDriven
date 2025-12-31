package com.example.specdriven.integration;

import com.example.specdriven.SpecDrivenApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify the Spring Boot application context loads successfully.
 * Tests Phase 1 and Phase 2 infrastructure setup.
 */
@SpringBootTest
class ApplicationContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext, "Application context should load successfully");
    }

    @Test
    void specDrivenApplication_IsLoaded() {
        assertNotNull(applicationContext.getBean(SpecDrivenApplication.class));
    }

    @Test
    void allRequiredBeans_ArePresent() {
        // Verify key beans from Phase 2 are configured
        assertTrue(applicationContext.containsBean("featureFlagConfig"));
        assertTrue(applicationContext.containsBean("featureFlagService"));
        assertTrue(applicationContext.containsBean("jwtConfig"));
        assertTrue(applicationContext.containsBean("globalExceptionHandler"));
        assertTrue(applicationContext.containsBean("passwordEncoder"));
    }
}
