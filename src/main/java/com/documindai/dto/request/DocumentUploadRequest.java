package com.documindai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

/**
 * DTO cho request upload tài liệu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {
    
    @NotNull(message = "File không được để trống")
    private MultipartFile file;
    
    private String description; // Mô tả tài liệu (optional)
    
    private Boolean autoProcess; // Tự động xử lý bởi AI sau khi upload (mặc định true)
}
