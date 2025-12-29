package com.example.specdriven;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the SpecDriven User Management API application.
 * 
 * This Spring Boot application provides a REST API for user management
 * with JWT-based authentication and role-based access control.
 */
@SpringBootApplication
public class SpecDrivenApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpecDrivenApplication.class, args);
    }
}
