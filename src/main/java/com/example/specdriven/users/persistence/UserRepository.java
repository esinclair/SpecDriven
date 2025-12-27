package com.example.specdriven.users.persistence;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, UUID> {

    @Query("SELECT COUNT(*) FROM users")
    long count();

    @Query("SELECT * FROM users WHERE username = :username")
    Optional<UserEntity> findByUsername(@Param("username") String username);

    @Query("SELECT * FROM users WHERE email_address = :email")
    Optional<UserEntity> findByEmailAddress(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM users WHERE email_address = :email")
    boolean existsByEmailAddress(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM users WHERE username = :username")
    boolean existsByUsername(@Param("username") String username);
}

