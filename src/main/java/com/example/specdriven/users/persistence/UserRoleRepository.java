package com.example.specdriven.users.persistence;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends CrudRepository<UserRoleEntity, Long> {

    @Query("SELECT * FROM user_roles WHERE user_id = :userId")
    Set<UserRoleEntity> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM user_roles WHERE user_id = :userId AND role_name = :roleName")
    boolean existsByUserIdAndRoleName(@Param("userId") UUID userId, @Param("roleName") String roleName);

    @Modifying
    @Query("DELETE FROM user_roles WHERE user_id = :userId AND role_name = :roleName")
    void deleteByUserIdAndRoleName(@Param("userId") UUID userId, @Param("roleName") String roleName);

    @Modifying
    @Query("DELETE FROM user_roles WHERE user_id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT user_id FROM user_roles WHERE role_name = :roleName")
    Set<UUID> findUserIdsByRoleName(@Param("roleName") String roleName);
}

