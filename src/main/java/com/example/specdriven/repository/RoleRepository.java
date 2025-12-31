package com.example.specdriven.repository;

import com.example.specdriven.domain.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Role entity persistence operations.
 * Roles are predefined in the system (ADMIN, USER, GUEST).
 */
@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    /**
     * Find a role by its name.
     * Role names are unique (ADMIN, USER, GUEST).
     *
     * @param roleName the role name to search for
     * @return Optional containing the role if found
     */
    Optional<RoleEntity> findByRoleName(String roleName);
}
