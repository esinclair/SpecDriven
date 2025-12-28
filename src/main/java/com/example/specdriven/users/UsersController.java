package com.example.specdriven.users;

import com.example.specdriven.api.UsersApi;
import com.example.specdriven.api.model.CreateUserRequest;
import com.example.specdriven.api.model.RoleName;
import com.example.specdriven.api.model.UpdateUserRequest;
import com.example.specdriven.api.model.User;
import com.example.specdriven.api.model.UserPage;
import com.example.specdriven.feature.UsersApiFeatureGate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Users API controller implementing generated OpenAPI interface.
 * Gated by feature-flag.users-api feature flag.
 * T029, T044, T051, T058: Controller implementation.
 */
@RestController
public class UsersController implements UsersApi {

    private final UsersApiFeatureGate featureGate;
    private final UsersService usersService;

    public UsersController(UsersApiFeatureGate featureGate, UsersService usersService) {
        this.featureGate = featureGate;
        this.usersService = usersService;
    }

    @Override
    public ResponseEntity<Void> assignRoleToUser(UUID userId, RoleName roleName) {
        return featureGate.ifEnabled("/users/" + userId + "/roles/" + roleName.getValue(), 
            () -> {
                usersService.assignRoleToUser(userId, roleName);
                return ResponseEntity.noContent().build();
            });
    }

    @Override
    public ResponseEntity<User> createUser(CreateUserRequest createUserRequest) {
        return featureGate.ifEnabled("/users", () -> {
            User user = usersService.createUser(createUserRequest);
            return ResponseEntity.status(201).body(user);
        });
    }

    @Override
    public ResponseEntity<Void> deleteUser(UUID userId) {
        return featureGate.ifEnabled("/users/" + userId, () -> {
            usersService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        });
    }

    @Override
    public ResponseEntity<User> getUserById(UUID userId) {
        return featureGate.ifEnabled("/users/" + userId, () -> {
            User user = usersService.getUserById(userId);
            return ResponseEntity.ok(user);
        });
    }

    @Override
    public ResponseEntity<UserPage> listUsers(Integer page, Integer pageSize, 
            String username, String emailAddress, String name, RoleName roleName) {
        return featureGate.ifEnabled("/users", () -> {
            UserPage userPage = usersService.listUsers(page, pageSize, username, emailAddress, name, roleName);
            return ResponseEntity.ok(userPage);
        });
    }

    @Override
    public ResponseEntity<Void> removeRoleFromUser(UUID userId, RoleName roleName) {
        return featureGate.ifEnabled("/users/" + userId + "/roles/" + roleName.getValue(), 
            () -> {
                usersService.removeRoleFromUser(userId, roleName);
                return ResponseEntity.noContent().build();
            });
    }

    @Override
    public ResponseEntity<User> updateUser(UUID userId, UpdateUserRequest updateUserRequest) {
        return featureGate.ifEnabled("/users/" + userId, () -> {
            User user = usersService.updateUser(userId, updateUserRequest);
            return ResponseEntity.ok(user);
        });
    }
}
