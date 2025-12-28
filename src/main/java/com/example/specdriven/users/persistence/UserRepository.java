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

    @Query("SELECT COUNT(*) FROM \"users\"")
    long count();

    @Query("SELECT * FROM \"users\" WHERE \"username\" = :username")
    Optional<UserEntity> findByUsername(@Param("username") String username);

    @Query("SELECT * FROM \"users\" WHERE \"email_address\" = :email")
    Optional<UserEntity> findByEmailAddress(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM \"users\" WHERE \"email_address\" = :email")
    boolean existsByEmailAddress(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM \"users\" WHERE \"username\" = :username")
    boolean existsByUsername(@Param("username") String username);

    // For pagination, we'll use simple queries that return all matching records
    // and handle pagination in the service layer
    @Query("SELECT * FROM \"users\"")
    List<UserEntity> findAllUsers();

    @Query("SELECT * FROM \"users\" WHERE \"id\" IN (:ids)")
    List<UserEntity> findByIdIn(@Param("ids") Set<UUID> ids);

    @Query("SELECT * FROM \"users\" WHERE \"username\" LIKE CONCAT('%', :username, '%')")
    List<UserEntity> findByUsernameContaining(@Param("username") String username);

    @Query("SELECT * FROM \"users\" WHERE \"email_address\" LIKE CONCAT('%', :email, '%')")
    List<UserEntity> findByEmailAddressContaining(@Param("email") String email);

    @Query("SELECT * FROM \"users\" WHERE LOWER(\"name\") LIKE LOWER(CONCAT('%', :name, '%'))")
    List<UserEntity> findByNameContainingIgnoreCase(@Param("name") String name);
}

