package com.documindai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request gửi tin nhắn chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    @Size(max = 5000, message = "Tin nhắn không được quá 5000 ký tự")
    private String message;
    
    private Long sessionId; // Optional: ID của chat session (null = tạo session mới)
    
    private Boolean useContext; // Có sử dụng context từ documents không (mặc định true)
    
    private Integer maxTokens; // Số tokens tối đa cho response (optional)
    
    private Double temperature; // Temperature cho AI (0.0 - 1.0, optional)
}
