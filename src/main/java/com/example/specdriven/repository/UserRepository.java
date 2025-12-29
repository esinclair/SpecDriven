package com.example.specdriven.repository;

import com.example.specdriven.domain.UserEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity operations.
 * Provides CRUD operations and custom queries for user management.
 */
@Repository
public interface UserRepository extends CrudRepository<UserEntity, UUID> {
    
    /**
     * Find a user by email address.
     * Used for login and email uniqueness checks.
     */
    @Query("SELECT * FROM users WHERE email_address = :email")
    Optional<UserEntity> findByEmailAddress(@Param("email") String email);
    
    /**
     * Find a user by username.
     * Used for filtering operations.
     */
    @Query("SELECT * FROM users WHERE username = :username")
    Optional<UserEntity> findByUsername(@Param("username") String username);
    
    /**
     * Count total number of users.
     * Used for bootstrap mode check (when count = 0, allow user creation without auth).
     */
    @Query("SELECT COUNT(*) FROM users")
    long countUsers();
    
    /**
     * Check if a user exists with the given email address.
     * Used for email uniqueness validation.
     */
    @Query("SELECT COUNT(*) > 0 FROM users WHERE email_address = :email AND id != :excludeId")
    boolean existsByEmailAddressExcludingId(@Param("email") String email, @Param("excludeId") UUID excludeId);
    
    /**
     * Check if a user exists with the given email address.
     * Used for email uniqueness validation during creation.
     */
    @Query("SELECT COUNT(*) > 0 FROM users WHERE email_address = :email")
    boolean existsByEmailAddress(@Param("email") String email);
    
    /**
     * Check if a user exists with the given username.
     * Used for username uniqueness validation.
     */
    @Query("SELECT COUNT(*) > 0 FROM users WHERE username = :username")
    boolean existsByUsername(@Param("username") String username);
}
