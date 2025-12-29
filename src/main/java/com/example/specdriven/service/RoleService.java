package com.example.specdriven.service;

import com.example.specdriven.domain.UserRoleEntity;
import com.example.specdriven.exception.ResourceNotFoundException;
import com.example.specdriven.exception.ValidationException;
import com.example.specdriven.repository.UserRepository;
import com.example.specdriven.repository.UserRoleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing user roles.
 */
@Service
public class RoleService {
    
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final JdbcTemplate jdbcTemplate;
    
    private static final List<String> VALID_ROLES = Arrays.asList("ADMIN", "USER", "VIEWER");
    
    public RoleService(UserRepository userRepository,
                      UserRoleRepository userRoleRepository,
                      JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Transactional
    public void assignRole(UUID userId, String roleName) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        
        // Validate role name
        if (!VALID_ROLES.contains(roleName)) {
            throw new ValidationException("Invalid role name");
        }
        
        // Check if role already assigned (idempotent)
        boolean exists = userRoleRepository.existsByUserIdAndRoleName(userId, roleName);
        if (!exists) {
            UserRoleEntity userRole = new UserRoleEntity();
            userRole.setUserId(userId);
            userRole.setRoleName(roleName);
            userRoleRepository.save(userRole);
        }
    }
    
    @Transactional
    public void removeRole(UUID userId, String roleName) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        
        // Remove role (idempotent - no error if not assigned)
        userRoleRepository.deleteByUserIdAndRoleName(userId, roleName);
    }
}
