package com.documindai.repository;

import com.documindai.model.Message;
import com.documindai.model.Message.MessageRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface cho Message entity
 * Quản lý các tin nhắn trong chat sessions
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * Tìm tất cả messages trong một chat session
     */
    List<Message> findByChatSessionId(Long sessionId);
    
    /**
     * Tìm messages trong chat session, sắp xếp theo thời gian
     */
    List<Message> findByChatSessionIdOrderByTimestampAsc(Long sessionId);
    
    /**
     * Tìm messages theo role (USER hoặc ASSISTANT)
     */
    List<Message> findByChatSessionIdAndRole(Long sessionId, MessageRole role);
    
    /**
     * Tìm messages trong khoảng thời gian
     */
    List<Message> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Đếm số lượng messages trong chat session
     */
    Long countByChatSessionId(Long sessionId);
    
    /**
     * Lấy message mới nhất trong chat session
     */
    Message findFirstByChatSessionIdOrderByTimestampDesc(Long sessionId);
    
    /**
     * Lấy N messages gần nhất trong chat session (cho context)
     */
    @Query("SELECT m FROM Message m WHERE m.chatSession.id = :sessionId " +
           "ORDER BY m.timestamp DESC")
    List<Message> findRecentMessages(@Param("sessionId") Long sessionId);
    
    /**
     * Tìm kiếm messages theo nội dung
     */
    @Query("SELECT m FROM Message m WHERE m.chatSession.id = :sessionId AND " +
           "m.content LIKE %:keyword% ORDER BY m.timestamp DESC")
    List<Message> searchMessages(@Param("sessionId") Long sessionId, 
                                 @Param("keyword") String keyword);
    
    /**
     * Tính tổng tokens đã sử dụng trong chat session
     */
    @Query("SELECT SUM(m.tokensUsed) FROM Message m WHERE m.chatSession.id = :sessionId")
    Long getTotalTokensUsed(@Param("sessionId") Long sessionId);
    
    /**
     * Tính tổng tokens đã sử dụng của user
     */
    @Query("SELECT SUM(m.tokensUsed) FROM Message m WHERE m.chatSession.user.id = :userId")
    Long getTotalTokensUsedByUser(@Param("userId") Long userId);
    
    /**
     * Xóa messages cũ của chat session
     */
    void deleteByChatSessionId(Long sessionId);
}
