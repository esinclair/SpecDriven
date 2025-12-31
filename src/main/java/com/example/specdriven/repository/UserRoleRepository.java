package com.example.specdriven.repository;

import com.example.specdriven.domain.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    @Query("SELECT ur FROM UserRoleEntity ur WHERE ur.userId = :userId")
    List<UserRoleEntity> findByUserId(@Param("userId") UUID userId);

    /**
     * Find a specific user-role mapping.
     *
     * @param userId the user ID
     * @param roleId the role ID
     * @return list containing the mapping if it exists
     */
    @Query("SELECT ur FROM UserRoleEntity ur WHERE ur.userId = :userId AND ur.roleId = :roleId")
    List<UserRoleEntity> findByUserIdAndRoleId(@Param("userId") UUID userId, 
                                                @Param("roleId") UUID roleId);

    /**
     * Delete a specific user-role mapping (for role removal).
     *
     * @param userId the user ID
     * @param roleId the role ID
     */
    @Modifying
    @Query("DELETE FROM UserRoleEntity ur WHERE ur.userId = :userId AND ur.roleId = :roleId")
    void deleteByUserIdAndRoleId(@Param("userId") UUID userId, @Param("roleId") UUID roleId);

    /**
     * Find all users with a specific role.
     *
     * @param roleId the role ID
     * @return list of user-role mappings for that role
     */
    @Query("SELECT ur FROM UserRoleEntity ur WHERE ur.roleId = :roleId")
    List<UserRoleEntity> findByRoleId(@Param("roleId") UUID roleId);
}
