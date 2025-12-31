package com.example.specdriven.controller;

import com.example.specdriven.api.UsersApi;
import com.example.specdriven.api.model.*;
import com.example.specdriven.service.RoleService;
import com.example.specdriven.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller implementing the Users API for user management operations.
 * Delegates to UserService and RoleService for business logic.
 */
@RestController
public class UsersController implements UsersApi {

    private final UserService userService;
    private final RoleService roleService;

    public UsersController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    /**
     * Create a new user.
     *
     * @param createUserRequest the user creation request
     * @return 201 Created with the new User
     */
    @Override
    public ResponseEntity<User> createUser(CreateUserRequest createUserRequest) {
        User user = userService.createUser(createUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * Get a user by ID.
     *
     * @param userId the user ID
     * @return 200 OK with the User
     */
    @Override
    public ResponseEntity<User> getUserById(UUID userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Update an existing user.
     *
     * @param userId the user ID
     * @param updateUserRequest the update request
     * @return 200 OK with the updated User
     */
    @Override
    public ResponseEntity<User> updateUser(UUID userId, UpdateUserRequest updateUserRequest) {
        User user = userService.updateUser(userId, updateUserRequest);
        return ResponseEntity.ok(user);
    }

    /**
     * Delete a user by ID.
     *
     * @param userId the user ID
     * @return 204 No Content
     */
    @Override
    public ResponseEntity<Void> deleteUser(UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * List users with pagination and optional filters.
     *
     * @param page 1-based page number
     * @param pageSize number of items per page
     * @param username optional username filter
     * @param emailAddress optional email filter
     * @param name optional name filter
     * @param roleName optional role filter
     * @return 200 OK with paginated user list
     */
    @Override
    public ResponseEntity<UserPage> listUsers(Integer page, Integer pageSize, String username,
                                              String emailAddress, String name, RoleName roleName) {
        UserPage userPage = userService.listUsers(page, pageSize, username, emailAddress, name, roleName);
        return ResponseEntity.ok(userPage);
    }

    /**
     * Assign a role to a user (idempotent).
     *
     * @param userId the user ID
     * @param roleName the role to assign
     * @return 204 No Content
     */
    @Override
    public ResponseEntity<Void> assignRoleToUser(UUID userId, RoleName roleName) {
        roleService.assignRole(userId, roleName);
        return ResponseEntity.noContent().build();
    }

    /**
     * Remove a role from a user (idempotent).
     *
     * @param userId the user ID
     * @param roleName the role to remove
     * @return 204 No Content
     */
    @Override
    public ResponseEntity<Void> removeRoleFromUser(UUID userId, RoleName roleName) {
        roleService.removeRole(userId, roleName);
        return ResponseEntity.noContent().build();
    }
}
