package com.example.specdriven.users.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T011: Persistence smoke test verifying schema + basic insert/select works on H2
 */
@SpringBootTest
@TestPropertySource(properties = {
        "feature-flag.users-api=true"
})
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void canInsertAndRetrieveUser() {
        // Given a user - insert directly via JDBC to test schema
        UUID userId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO \"USERS\" (\"ID\", \"USERNAME\", \"NAME\", \"EMAIL_ADDRESS\", \"PASSWORD_HASH\", \"CREATED_AT\", \"UPDATED_AT\") VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                userId, "testuser", "Test User", "test@example.com", "$2a$10$dummyhash"
        );

        // Then we can retrieve it via repository
        Optional<UserEntity> retrieved = userRepository.findById(userId);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getUsername()).isEqualTo("testuser");
        assertThat(retrieved.get().getName()).isEqualTo("Test User");
        assertThat(retrieved.get().getEmailAddress()).isEqualTo("test@example.com");
        assertThat(retrieved.get().getPasswordHash()).isEqualTo("$2a$10$dummyhash");
    }

    @Test
    void canCountUsers() {
        // Given no users initially (test isolation via @Transactional)
        long initialCount = userRepository.count();

        // When we insert a user via JDBC
        jdbcTemplate.update(
                "INSERT INTO \"USERS\" (\"ID\", \"USERNAME\", \"NAME\", \"EMAIL_ADDRESS\", \"PASSWORD_HASH\", \"CREATED_AT\", \"UPDATED_AT\") VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                UUID.randomUUID(), "counttest", "Count Test", "count@example.com", "$2a$10$dummyhash"
        );

        // Then count increases
        long newCount = userRepository.count();
        assertThat(newCount).isEqualTo(initialCount + 1);
    }

    @Test
    void canFindByEmailAddress() {
        // Given a user
        UUID userId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO \"USERS\" (\"ID\", \"USERNAME\", \"NAME\", \"EMAIL_ADDRESS\", \"PASSWORD_HASH\", \"CREATED_AT\", \"UPDATED_AT\") VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                userId, "emailtest", "Email Test", "unique@example.com", "$2a$10$dummyhash"
        );

        // When we find by email
        Optional<UserEntity> found = userRepository.findByEmailAddress("unique@example.com");

        // Then we get the user
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("emailtest");
    }

    @Test
    void findByEmailAddress_returnsEmpty_whenNotFound() {
        Optional<UserEntity> found = userRepository.findByEmailAddress("nonexistent@example.com");
        assertThat(found).isEmpty();
    }
}
