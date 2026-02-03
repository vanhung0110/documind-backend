package com.documindai.service;

import com.documindai.dto.request.LoginRequest;
import com.documindai.dto.request.RegisterRequest;
import com.documindai.dto.response.AuthResponse;

/**
 * Service interface cho authentication
 */
public interface AuthService {

    /**
     * Đăng nhập
     */
    AuthResponse login(LoginRequest request);

    /**
     * Đăng ký user mới
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Tạo admin mặc định nếu chưa tồn tại
     */
    void createDefaultAdmin();
}
