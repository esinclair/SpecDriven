package com.example.specdriven.repository;

import com.example.specdriven.domain.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity persistence operations.
 * Provides CRUD operations, pagination, and custom query methods.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Find a user by email address.
     * Email addresses are unique in the system.
     *
     * @param emailAddress the email address to search for
     * @return Optional containing the user if found
     */
    Optional<UserEntity> findByEmailAddress(String emailAddress);

    /**
     * Find a user by username.
     *
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Count total number of users in the system.
     *
     * @return total user count
     */
    long count();

    /**
     * Find all users with pagination.
     *
     * @param pageable pagination parameters
     * @return page of users
     */
    Page<UserEntity> findAll(Pageable pageable);

    /**
     * Find users by exact username match with pagination.
     *
     * @param username the username to match
     * @param pageable pagination parameters
     * @return page of matching users
     */
    Page<UserEntity> findByUsername(String username, Pageable pageable);

    /**
     * Find users by exact email address match with pagination.
     *
     * @param emailAddress the email address to match
     * @param pageable pagination parameters
     * @return page of matching users
     */
    Page<UserEntity> findByEmailAddress(String emailAddress, Pageable pageable);

    /**
     * Find users by case-insensitive partial name match with pagination.
     *
     * @param name the name substring to match
     * @param pageable pagination parameters
     * @return page of matching users
     */
    Page<UserEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Find users by username and name filter with pagination.
     *
     * @param username the username to match exactly
     * @param name the name substring to match (case-insensitive)
     * @param pageable pagination parameters
     * @return page of matching users
     */
    Page<UserEntity> findByUsernameAndNameContainingIgnoreCase(String username, String name, Pageable pageable);

    /**
     * Find users by username and email address with pagination.
     *
     * @param username the username to match exactly
     * @param emailAddress the email address to match exactly
     * @param pageable pagination parameters
     * @return page of matching users
     */
    Page<UserEntity> findByUsernameAndEmailAddress(String username, String emailAddress, Pageable pageable);

    /**
     * Find users by email address and name filter with pagination.
     *
     * @param emailAddress the email address to match exactly
     * @param name the name substring to match (case-insensitive)
     * @param pageable pagination parameters
     * @return page of matching users
     */
    Page<UserEntity> findByEmailAddressAndNameContainingIgnoreCase(String emailAddress, String name, Pageable pageable);

    /**
     * Find users by all filters with pagination.
     *
     * @param username the username to match exactly
     * @param emailAddress the email address to match exactly
     * @param name the name substring to match (case-insensitive)
     * @param pageable pagination parameters
     * @return page of matching users
     */
    Page<UserEntity> findByUsernameAndEmailAddressAndNameContainingIgnoreCase(
            String username, String emailAddress, String name, Pageable pageable);

    /**
     * Find users by IDs with pagination.
     *
     * @param userIds list of user IDs to find
     * @param pageable pagination parameters
     * @return page of matching users
     */
    Page<UserEntity> findByIdIn(List<UUID> userIds, Pageable pageable);
}
