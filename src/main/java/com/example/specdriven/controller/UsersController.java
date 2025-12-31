package com.example.specdriven.controller;

import com.example.specdriven.api.UsersApi;
import com.example.specdriven.api.model.*;
import com.example.specdriven.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for user management endpoints.
 * Implements the generated UsersApi interface from OpenAPI contract.
 */
@RestController
public class UsersController implements UsersApi {
    
    private final UserService userService;
    
    public UsersController(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public ResponseEntity<User> createUser(@Valid CreateUserRequest createUserRequest) {
        User user = userService.createUser(createUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    @Override
    public ResponseEntity<User> getUserById(UUID id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @Override
    public ResponseEntity<User> updateUser(UUID id, @Valid UpdateUserRequest updateUserRequest) {
        User user = userService.updateUser(id, updateUserRequest);
        return ResponseEntity.ok(user);
    }
    
    @Override
    public ResponseEntity<Void> deleteUser(UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    @Override
    public ResponseEntity<Void> assignRoleToUser(UUID id, RoleName roleName) {
        // TODO: Implement in Phase 9 - Role Management
        throw new UnsupportedOperationException("Role management not yet implemented");
    }
    
    @Override
    public ResponseEntity<Void> removeRoleFromUser(UUID id, RoleName roleName) {
        // TODO: Implement in Phase 9 - Role Management
        throw new UnsupportedOperationException("Role management not yet implemented");
    }
    
    @Override
    public ResponseEntity<UserPage> listUsers(
            @Valid @Min(1) Integer page,
            @Valid @Min(1) @Max(100) Integer pageSize,
            String username,
            String emailAddress,
            String name,
            RoleName roleName) {
        // TODO: Implement in Phase 8 - List and Filter Users
        throw new UnsupportedOperationException("List users not yet implemented");
    }
}
