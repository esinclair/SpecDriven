package com.example.specdriven.service;

import com.example.specdriven.api.model.User;
import com.example.specdriven.domain.RoleEntity;
import com.example.specdriven.domain.UserEntity;
import com.example.specdriven.domain.UserRoleEntity;
import com.example.specdriven.exception.ConflictException;
import com.example.specdriven.exception.ResourceNotFoundException;
import com.example.specdriven.exception.ValidationException;
import com.example.specdriven.mapper.UserMapper;
import com.example.specdriven.repository.UserRepository;
import com.example.specdriven.repository.UserRoleRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing users.
 */
@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserMapper userMapper;
    private final JdbcTemplate jdbcTemplate;
    
    public UserService(UserRepository userRepository,
                      UserRoleRepository userRoleRepository,
                      UserMapper userMapper,
                      JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.userMapper = userMapper;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Transactional
    public User createUser(User user, String password) {
        // Validate required fields
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new ValidationException("Username is required");
        }
        if (user.getEmailAddress() == null || user.getEmailAddress().isBlank()) {
            throw new ValidationException("Email address is required");
        }
        if (password == null || password.isBlank()) {
            throw new ValidationException("Password is required");
        }
        
        // Check for duplicate email
        if (userRepository.existsByEmailAddress(user.getEmailAddress())) {
            throw new ConflictException("Email address already exists");
        }
        
        // Check for duplicate username
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ConflictException("Username already exists");
        }
        
        // Create entity
        UserEntity entity = userMapper.toEntity(user, password);
        UserEntity saved = userRepository.save(entity);
        
        // Load and set roles
        saved.setRoles(loadRolesForUser(saved.getId()));
        
        return userMapper.toDto(saved);
    }
    
    public User getUserById(UUID userId) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Load roles
        entity.setRoles(loadRolesForUser(userId));
        
        return userMapper.toDto(entity);
    }
    
    @Transactional
    public User updateUser(UUID userId, User user, String password) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check for duplicate email if changing
        if (user.getEmailAddress() != null && 
                !user.getEmailAddress().equals(entity.getEmailAddress())) {
            if (userRepository.existsByEmailAddress(user.getEmailAddress())) {
                throw new ConflictException("Email address already exists");
            }
        }
        
        // Check for duplicate username if changing
        if (user.getUsername() != null && 
                !user.getUsername().equals(entity.getUsername())) {
            if (userRepository.existsByUsername(user.getUsername())) {
                throw new ConflictException("Username already exists");
            }
        }
        
        userMapper.updateEntity(entity, user, password);
        UserEntity updated = userRepository.save(entity);
        
        // Load roles
        updated.setRoles(loadRolesForUser(userId));
        
        return userMapper.toDto(updated);
    }
    
    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        
        // Delete user roles first
        userRoleRepository.deleteByUserId(userId);
        
        // Delete user
        userRepository.deleteById(userId);
    }
    
    public Map<String, Object> listUsers(Integer page, Integer pageSize, 
                                        String username, String emailAddress, 
                                        String name, String roleName) {
        // Validate pagination parameters
        if (page == null || page < 1) {
            throw new ValidationException("Page must be >= 1");
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            throw new ValidationException("Page size must be between 1 and 100");
        }
        
        // Build query
        StringBuilder sql = new StringBuilder("SELECT DISTINCT u.* FROM users u");
        List<Object> params = new ArrayList<>();
        
        // Join with roles if filtering by role
        if (roleName != null && !roleName.isBlank()) {
            sql.append(" JOIN user_roles ur ON u.id = ur.user_id");
        }
        
        // Build WHERE clause
        List<String> conditions = new ArrayList<>();
        if (username != null && !username.isBlank()) {
            conditions.add("u.username = ?");
            params.add(username);
        }
        if (emailAddress != null && !emailAddress.isBlank()) {
            conditions.add("u.email_address = ?");
            params.add(emailAddress);
        }
        if (name != null && !name.isBlank()) {
            conditions.add("LOWER(u.name) LIKE LOWER(?)");
            params.add("%" + name + "%");
        }
        if (roleName != null && !roleName.isBlank()) {
            conditions.add("ur.role_name = ?");
            params.add(roleName);
        }
        
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        
        // Count total
        String countSql = "SELECT COUNT(DISTINCT u.id) FROM (" + sql.toString() + ") AS subquery";
        long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());
        
        // Add pagination
        sql.append(" ORDER BY u.created_at DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
        
        // Query users
        List<UserEntity> entities = jdbcTemplate.query(sql.toString(), params.toArray(), new UserRowMapper());
        
        // Load roles for each user
        for (UserEntity entity : entities) {
            entity.setRoles(loadRolesForUser(entity.getId()));
        }
        
        // Convert to DTOs
        List<User> users = entities.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
        
        // Calculate pagination metadata
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        
        Map<String, Object> result = new HashMap<>();
        result.put("items", users);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalCount", totalCount);
        result.put("totalPages", totalPages);
        
        return result;
    }
    
    public UserEntity findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    private List<RoleEntity> loadRolesForUser(UUID userId) {
        String sql = "SELECT r.* FROM roles r " +
                    "JOIN user_roles ur ON r.role_name = ur.role_name " +
                    "WHERE ur.user_id = ?";
        return jdbcTemplate.query(sql, new RoleRowMapper(), userId);
    }
    
    private static class UserRowMapper implements RowMapper<UserEntity> {
        @Override
        public UserEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new UserEntity(
                    UUID.fromString(rs.getString("id")),
                    rs.getString("username"),
                    rs.getString("name"),
                    rs.getString("email_address"),
                    rs.getString("password_hash"),
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant()
            );
        }
    }
    
    private static class RoleRowMapper implements RowMapper<RoleEntity> {
        @Override
        public RoleEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            RoleEntity role = new RoleEntity();
            role.setId(UUID.fromString(rs.getString("id")));
            role.setName(rs.getString("role_name"));
            role.setDescription(rs.getString("description"));
            return role;
        }
    }
}
