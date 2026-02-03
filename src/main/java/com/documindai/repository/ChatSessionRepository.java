package com.documindai.repository;

import com.documindai.model.ChatSession;
import com.documindai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho ChatSession entity
 * Quản lý các phiên chat giữa user và AI
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

       /**
        * Tìm tất cả chat sessions của một user
        */
       List<ChatSession> findByUser(User user);

       /**
        * Tìm chat sessions theo user ID
        */
       List<ChatSession> findByUserId(Long userId);

       /**
        * Tìm chat sessions đang active của user
        */
       List<ChatSession> findByUserIdAndActiveTrue(Long userId);

       /**
        * Tìm chat sessions theo user, sắp xếp theo thời gian mới nhất
        */
       List<ChatSession> findByUserIdOrderByUpdatedAtDesc(Long userId);

       /**
        * Tìm chat session mới nhất của user
        */
       Optional<ChatSession> findFirstByUserIdOrderByUpdatedAtDesc(Long userId);

       /**
        * Tìm chat sessions được tạo trong khoảng thời gian
        */
       List<ChatSession> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

       /**
        * Đếm số lượng chat sessions của user
        */
       Long countByUserId(Long userId);

       /**
        * Đếm tổng số chat sessions active
        */
       Long countByActiveTrue();

       /**
        * Tìm kiếm chat sessions theo title
        */
       @Query("SELECT cs FROM ChatSession cs WHERE cs.userId = :userId AND " +
                     "cs.title LIKE %:keyword% ORDER BY cs.updatedAt DESC")
       List<ChatSession> searchChatSessions(@Param("userId") Long userId,
                     @Param("keyword") String keyword);

       /**
        * Lấy chat sessions gần đây nhất của user (giới hạn số lượng)
        */
       List<ChatSession> findTop10ByUserIdAndActiveTrueOrderByUpdatedAtDesc(Long userId);

       /**
        * Xóa các chat sessions cũ (inactive và quá 30 ngày)
        */
       @Query("SELECT cs FROM ChatSession cs WHERE cs.active = false AND " +
                     "cs.updatedAt < :cutoffDate")
       List<ChatSession> findOldInactiveSessions(@Param("cutoffDate") LocalDateTime cutoffDate);

       /**
        * Tìm chat sessions theo user ID và active status, sắp xếp theo lastMessageAt
        */
       List<ChatSession> findByUserIdAndActiveOrderByLastMessageAtDesc(Long userId, boolean active);
}
