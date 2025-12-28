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
public interface UserRoleRepository extends CrudRepository<UserRoleEntity, UUID> {

    @Query("SELECT * FROM \"USER_ROLES\" WHERE \"USER_ID\" = :userId")
    Set<UserRoleEntity> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM \"USER_ROLES\" WHERE \"USER_ID\" = :userId AND \"ROLE_NAME\" = :roleName")
    boolean existsByUserIdAndRoleName(@Param("userId") UUID userId, @Param("roleName") String roleName);

    @Modifying
    @Query("DELETE FROM \"USER_ROLES\" WHERE \"USER_ID\" = :userId AND \"ROLE_NAME\" = :roleName")
    void deleteByUserIdAndRoleName(@Param("userId") UUID userId, @Param("roleName") String roleName);

    @Modifying
    @Query("DELETE FROM \"USER_ROLES\" WHERE \"USER_ID\" = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT \"USER_ID\" FROM \"USER_ROLES\" WHERE \"ROLE_NAME\" = :roleName")
    Set<UUID> findUserIdsByRoleName(@Param("roleName") String roleName);
}

