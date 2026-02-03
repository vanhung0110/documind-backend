package com.documindai.repository;

import com.documindai.model.Role;
import com.documindai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho User entity
 * Cung cấp các phương thức truy vấn database cho User
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tìm user theo username
     */
    Optional<User> findByUsername(String username);

    /**
     * Tìm user theo email
     */
    Optional<User> findByEmail(String email);

    /**
     * Kiểm tra username đã tồn tại chưa
     */
    Boolean existsByUsername(String username);

    /**
     * Kiểm tra email đã tồn tại chưa
     */
    Boolean existsByEmail(String email);

    /**
     * Tìm tất cả users theo role
     */
    List<User> findByRole(Role role);

    /**
     * Tìm tất cả users đang active
     */
    List<User> findByActiveTrue();

    /**
     * Tìm users theo role và active status
     */
    List<User> findByRoleAndActive(Role role, Boolean active);

    /**
     * Đếm số lượng users theo role
     */
    Long countByRole(Role role);

    /**
     * Tìm kiếm users theo username hoặc email (LIKE)
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %?1% OR u.email LIKE %?1%")
    List<User> searchUsers(String keyword);

    /**
     * Tìm users theo active status, sắp xếp theo ngày tạo
     */
    List<User> findByActiveOrderByCreatedAtDesc(boolean active);

    /**
     * Đếm số users theo active status
     */
    long countByActive(boolean active);

    /**
     * Đếm số users theo role và active status
     */
    long countByRoleAndActive(Role role, boolean active);
}
