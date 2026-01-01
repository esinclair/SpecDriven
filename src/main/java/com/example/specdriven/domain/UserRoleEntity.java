package com.example.specdriven.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity representing user-role mappings.
 * Maps to the 'user_roles' join table in the database.
 * Uses composite primary key (user_id, role_id).
 */
@Entity
@Table(name = "user_roles")
@IdClass(UserRoleEntity.UserRoleId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "role_id")
    private UUID roleId;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    /**
     * Composite primary key class for UserRoleEntity.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class UserRoleId implements Serializable {
        private UUID userId;
        private UUID roleId;
    }
}
