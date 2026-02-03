package com.documindai.service;

import com.documindai.dto.response.UserResponse;
import com.documindai.model.User;

import java.util.List;

/**
 * Service interface cho user management
 */
public interface UserService {

    /**
     * Lấy user theo username
     */
    User getUserByUsername(String username);

    /**
     * Lấy user theo ID
     */
    User getUserById(Long id);

    /**
     * Lấy tất cả users
     */
    List<UserResponse> getAllUsers();

    /**
     * Kiểm tra username đã tồn tại
     */
    boolean existsByUsername(String username);

    /**
     * Kiểm tra email đã tồn tại
     */
    boolean existsByEmail(String email);

    /**
     * Đếm tổng số users
     */
    long countUsers();

    /**
     * Đếm số users theo role
     */
    long countUsersByRole(String role);
}
