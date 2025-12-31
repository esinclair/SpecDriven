package com.example.specdriven.repository;

import com.example.specdriven.domain.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity persistence operations.
 * Provides CRUD operations, pagination, and custom query methods.
 */
@Repository
public interface UserRepository extends CrudRepository<UserEntity, UUID>, 
                                       PagingAndSortingRepository<UserEntity, UUID> {

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
}
