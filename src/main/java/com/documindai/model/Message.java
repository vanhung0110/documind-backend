package com.documindai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho một tin nhắn trong phiên chat
 */
@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // Nội dung tin nhắn

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageRole role; // USER hoặc ASSISTANT

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String context; // Context từ documents được sử dụng để trả lời

    @Column
    private Integer tokensUsed; // Số tokens đã sử dụng cho message này

    @Column(columnDefinition = "TEXT")
    private String sourceDocuments; // JSON array của document IDs được sử dụng

    @Column
    private Double confidenceScore; // Độ tin cậy của câu trả lời (0-1)

    // Quan hệ với ChatSession
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession chatSession;

    /**
     * Enum cho vai trò của message
     */
    public enum MessageRole {
        USER, // Tin nhắn từ người dùng
        ASSISTANT // Tin nhắn từ AI
    }
}
