package com.example.specdriven.repository;

import com.example.specdriven.domain.UserRoleEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for UserRole entity operations.
 * Manages user-role assignments (many-to-many relationship).
 */
@Repository
public interface UserRoleRepository extends CrudRepository<UserRoleEntity, UUID> {
    
    /**
     * Find all role names assigned to a specific user.
     */
    @Query("SELECT role_name FROM user_roles WHERE user_id = :userId")
    Iterable<String> findRoleNamesByUserId(@Param("userId") UUID userId);
    
    /**
     * Check if a user has a specific role assigned.
     */
    @Query("SELECT COUNT(*) > 0 FROM user_roles WHERE user_id = :userId AND role_name = :roleName")
    boolean existsByUserIdAndRoleName(@Param("userId") UUID userId, @Param("roleName") String roleName);
    
    /**
     * Delete a specific user-role assignment.
     * Used for removing roles from users.
     */
    @Modifying
    @Query("DELETE FROM user_roles WHERE user_id = :userId AND role_name = :roleName")
    void deleteByUserIdAndRoleName(@Param("userId") UUID userId, @Param("roleName") String roleName);
    
    /**
     * Delete all role assignments for a user.
     * Used during user deletion (CASCADE should handle this, but explicit method available).
     */
    @Modifying
    @Query("DELETE FROM user_roles WHERE user_id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}
