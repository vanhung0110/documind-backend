package com.documindai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho response thông tin message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    
    private Long id;
    private String content;
    private String role; // USER hoặc ASSISTANT
    private LocalDateTime timestamp;
    private Integer tokensUsed;
}
