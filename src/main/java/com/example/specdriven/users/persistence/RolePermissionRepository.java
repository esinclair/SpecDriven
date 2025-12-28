package com.example.specdriven.users.persistence;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends CrudRepository<RolePermissionEntity, UUID> {

    @Query("SELECT * FROM \"ROLE_PERMISSIONS\" WHERE \"ROLE_NAME\" = :roleName")
    List<RolePermissionEntity> findByRoleName(@Param("roleName") String roleName);
}

