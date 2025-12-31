package com.example.specdriven.service;

import com.example.specdriven.api.model.RoleName;
import com.example.specdriven.domain.RoleEntity;
import com.example.specdriven.domain.UserRoleEntity;
import com.example.specdriven.exception.ResourceNotFoundException;
import com.example.specdriven.exception.ValidationException;
import com.example.specdriven.repository.RoleRepository;
import com.example.specdriven.repository.UserRepository;
import com.example.specdriven.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing user roles.
 * Handles role assignment and removal with idempotent operations.
 */
@Service
public class RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

    /**
     * Set of valid role names as defined in the RoleName enum.
     */
    private static final Set<String> VALID_ROLE_NAMES = Arrays.stream(RoleName.values())
            .map(RoleName::getValue)
            .collect(Collectors.toSet());

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public RoleService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * Assign a role to a user (idempotent operation).
     * If the role is already assigned, this operation is a no-op.
     *
     * @param userId the user ID
     * @param roleName the role to assign
     * @throws ResourceNotFoundException if user not found
     * @throws ValidationException if role name is invalid
     */
    @Transactional
    public void assignRole(UUID userId, RoleName roleName) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        // Get role name value
        String roleNameValue = roleName.getValue();

        // Validate role name
        validateRoleName(roleNameValue);

        // Find the role entity
        RoleEntity role = roleRepository.findByRoleName(roleNameValue)
                .orElseThrow(() -> new ValidationException("Role not found: " + roleNameValue));

        // Check if already assigned
        List<UserRoleEntity> existingAssignment = userRoleRepository.findByUserIdAndRoleId(userId, role.getId());
        if (!existingAssignment.isEmpty()) {
            logger.debug("Role {} already assigned to user {}, operation is idempotent", roleNameValue, userId);
            return;
        }

        // Create new assignment
        UserRoleEntity userRole = new UserRoleEntity(userId, role.getId(), LocalDateTime.now());
        userRoleRepository.save(userRole);

        logger.info("Assigned role {} to user {}", roleNameValue, userId);
    }

    /**
     * Remove a role from a user (idempotent operation).
     * If the role is not assigned, this operation is a no-op.
     *
     * @param userId the user ID
     * @param roleName the role to remove
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public void removeRole(UUID userId, RoleName roleName) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        // Get role name value
        String roleNameValue = roleName.getValue();

        // Find the role entity (if role doesn't exist, nothing to remove)
        RoleEntity role = roleRepository.findByRoleName(roleNameValue).orElse(null);
        if (role == null) {
            logger.debug("Role {} not found, nothing to remove from user {}", roleNameValue, userId);
            return;
        }

        // Delete the assignment if it exists
        userRoleRepository.deleteByUserIdAndRoleId(userId, role.getId());

        logger.info("Removed role {} from user {} (if it was assigned)", roleNameValue, userId);
    }

    /**
     * Validate that a role name is in the predefined set.
     *
     * @param roleName the role name to validate
     * @throws ValidationException if role name is not valid
     */
    private void validateRoleName(String roleName) {
        if (!VALID_ROLE_NAMES.contains(roleName)) {
            throw new ValidationException("Invalid role name: " + roleName + 
                    ". Valid values are: " + VALID_ROLE_NAMES);
        }
    }
}
