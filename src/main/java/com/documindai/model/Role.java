package com.documindai.model;

/**
 * Enum định nghĩa các vai trò trong hệ thống
 */
public enum Role {
    ROLE_ADMIN,  // Quản trị viên - có quyền upload tài liệu, quản lý users
    ROLE_USER    // Người dùng thông thường - chỉ có quyền chat
}
    