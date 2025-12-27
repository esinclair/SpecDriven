package com.example.specdriven.users.persistence;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends CrudRepository<RolePermissionEntity, Long> {

    @Query("SELECT * FROM role_permissions WHERE role_name = :roleName")
    List<RolePermissionEntity> findByRoleName(@Param("roleName") String roleName);
}

