package com.documindai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho response thông tin document
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private Long id;
    private String filename;
    private String originalFilename;
    private String fileType;
    private Long fileSize;
    private String fileSizeFormatted; // VD: "2.5 MB"
    private String summary;
    private Boolean processed;
    private LocalDateTime uploadDate;
    private String uploadedByUsername;
    private Long uploadedById;
    private Boolean active;
    private Integer contentLength; // Độ dài nội dung đã extract

    // Alias for uploadedByUsername (backward compatibility)
    public String getUploadedBy() {
        return uploadedByUsername;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedByUsername = uploadedBy;
    }
}
