package com.example.specdriven;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the SpecDriven User Management API System.
 *
 * This class bootstraps the Spring Boot application with auto-configuration,
 * component scanning, and configuration properties.
 */
@SpringBootApplication
public class SpecDrivenApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpecDrivenApplication.class, args);
    }
}

