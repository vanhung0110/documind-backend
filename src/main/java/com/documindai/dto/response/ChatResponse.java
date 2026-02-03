package com.documindai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho response tin nhắn chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private Long sessionId;
    private String message;
    private String role; // USER or ASSISTANT
    private LocalDateTime timestamp;
    private Double confidenceScore;
    private List<String> sourceDocuments; // Danh sách documents được sử dụng
}
