package com.example.specdriven.controller;

import com.example.specdriven.api.UsersApi;
import com.example.specdriven.api.model.CreateUserRequest;
import com.example.specdriven.api.model.RoleName;
import com.example.specdriven.api.model.UpdateUserRequest;
import com.example.specdriven.api.model.User;
import com.example.specdriven.api.model.UserPage;
import com.example.specdriven.service.RoleService;
import com.example.specdriven.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller implementing user management endpoints.
 */
@RestController
public class UsersController implements UsersApi {
    
    private final UserService userService;
    private final RoleService roleService;
    
    public UsersController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }
    
    @Override
    public ResponseEntity<User> createUser(CreateUserRequest createUserRequest) {
        User user = new User();
        user.setUsername(createUserRequest.getUsername());
        user.setName(createUserRequest.getName());
        user.setEmailAddress(createUserRequest.getEmailAddress());
        
        User created = userService.createUser(user, createUserRequest.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @Override
    public ResponseEntity<User> getUserById(UUID userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    
    @Override
    public ResponseEntity<User> updateUser(UUID userId, UpdateUserRequest updateUserRequest) {
        User user = new User();
        user.setUsername(updateUserRequest.getUsername());
        user.setName(updateUserRequest.getName());
        user.setEmailAddress(updateUserRequest.getEmailAddress());
        
        User updated = userService.updateUser(userId, user, updateUserRequest.getPassword());
        return ResponseEntity.ok(updated);
    }
    
    @Override
    public ResponseEntity<Void> deleteUser(UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
    
    @Override
    public ResponseEntity<UserPage> listUsers(Integer page, Integer pageSize, 
                                              String username, String emailAddress, 
                                              String name, RoleName roleName) {
        String roleNameStr = roleName != null ? roleName.getValue() : null;
        Map<String, Object> result = userService.listUsers(page, pageSize, username, 
                                                          emailAddress, name, roleNameStr);
        
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) result.get("items");
        
        UserPage userPage = new UserPage();
        userPage.setItems(users);
        userPage.setPage((Integer) result.get("page"));
        userPage.setPageSize((Integer) result.get("pageSize"));
        userPage.setTotalItems(((Long) result.get("totalCount")).intValue());
        userPage.setTotalPages((Integer) result.get("totalPages"));
        
        return ResponseEntity.ok(userPage);
    }
    
    @Override
    public ResponseEntity<Void> assignRoleToUser(UUID userId, RoleName roleName) {
        roleService.assignRole(userId, roleName.getValue());
        return ResponseEntity.noContent().build();
    }
    
    @Override
    public ResponseEntity<Void> removeRoleFromUser(UUID userId, RoleName roleName) {
        roleService.removeRole(userId, roleName.getValue());
        return ResponseEntity.noContent().build();
    }
}
