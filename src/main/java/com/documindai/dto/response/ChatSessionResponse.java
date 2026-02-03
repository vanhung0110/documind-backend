package com.documindai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho response thông tin chat session
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionResponse {
    
    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
    private Long userId;
    private Integer messageCount;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private List<MessageResponse> messages; // Danh sách messages (optional)
}
    