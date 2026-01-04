package com.example.specdriven.repository;

import com.example.specdriven.domain.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Permission entity persistence operations.
 */
@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {

    /**
     * Find all permissions for a specific user ID.
     * Joins users -> user_roles -> roles -> role_permissions -> permissions.
     *
     * @param userId the user ID
     * @return list of permission entities
     */
    @Query(value = """
            SELECT DISTINCT p.* FROM permissions p
            JOIN role_permissions rp ON p.id = rp.permission_id
            JOIN user_roles ur ON rp.role_id = ur.role_id
            WHERE ur.user_id = :userId
            """, nativeQuery = true)
    List<PermissionEntity> findByUserId(UUID userId);
}
