package com.example.specdriven.users.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, UUID> {

    @Query("SELECT COUNT(*) FROM \"USERS\"")
    long count();

    @Query("SELECT * FROM \"USERS\" WHERE \"USERNAME\" = :username")
    Optional<UserEntity> findByUsername(@Param("username") String username);

    @Query("SELECT * FROM \"USERS\" WHERE \"EMAIL_ADDRESS\" = :email")
    Optional<UserEntity> findByEmailAddress(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM \"USERS\" WHERE \"EMAIL_ADDRESS\" = :email")
    boolean existsByEmailAddress(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM \"USERS\" WHERE \"USERNAME\" = :username")
    boolean existsByUsername(@Param("username") String username);

    // For pagination, we'll use simple queries that return all matching records
    // and handle pagination in the service layer
    @Query("SELECT * FROM \"USERS\"")
    List<UserEntity> findAllUsers();

    @Query("SELECT * FROM \"USERS\" WHERE \"ID\" IN (:ids)")
    List<UserEntity> findByIdIn(@Param("ids") Set<UUID> ids);

    @Query("SELECT * FROM \"USERS\" WHERE \"USERNAME\" LIKE CONCAT('%', :username, '%')")
    List<UserEntity> findByUsernameContaining(@Param("username") String username);

    @Query("SELECT * FROM \"USERS\" WHERE \"EMAIL_ADDRESS\" LIKE CONCAT('%', :email, '%')")
    List<UserEntity> findByEmailAddressContaining(@Param("email") String email);

    @Query("SELECT * FROM \"USERS\" WHERE LOWER(\"NAME\") LIKE LOWER(CONCAT('%', :name, '%'))")
    List<UserEntity> findByNameContainingIgnoreCase(@Param("name") String name);
}

