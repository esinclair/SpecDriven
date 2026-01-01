package com.example.specdriven.repository;

import com.example.specdriven.domain.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for UserRole entity persistence operations.
 * Manages user-role mappings for role-based access control.
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UUID> {

    /**
     * Find all role assignments for a specific user.
     *
     * @param userId the user ID
     * @return list of user-role mappings
     */
    List<UserRoleEntity> findByUserId(UUID userId);

    /**
     * Find a specific user-role mapping.
     *
     * @param userId the user ID
     * @param roleId the role ID
     * @return list containing the mapping if it exists
     */
    List<UserRoleEntity> findByUserIdAndRoleId(UUID userId, UUID roleId);

    /**
     * Delete a specific user-role mapping (for role removal).
     *
     * @param userId the user ID
     * @param roleId the role ID
     */
    void deleteByUserIdAndRoleId(UUID userId, UUID roleId);

    /**
     * Find all users with a specific role.
     *
     * @param roleId the role ID
     * @return list of user-role mappings for that role
     */
    List<UserRoleEntity> findByRoleId(UUID roleId);
}
