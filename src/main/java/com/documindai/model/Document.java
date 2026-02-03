package com.documindai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho tài liệu được upload vào hệ thống
 */
@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String filename;
    
    @Column(nullable = false, length = 255)
    private String originalFilename;
    
    @Column(nullable = false, length = 500)
    private String filePath;
    
    @Column(nullable = false, length = 50)
    private String fileType; // pdf, doc, docx, txt
    
    @Column(nullable = false)
    private Long fileSize; // Kích thước file (bytes)
    
    @Column(columnDefinition = "LONGTEXT")
    private String extractedContent; // Nội dung text đã extract từ file
    
    @Column(columnDefinition = "TEXT")
    private String summary; // Tóm tắt nội dung do AI tạo
    
    @Column(columnDefinition = "LONGTEXT")
    private String embedding; // Vector embedding từ OpenAI (JSON format)
    
    @Column(nullable = false)
    private Boolean processed = false; // Đã xử lý bởi AI chưa
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadDate;
    
    // Quan hệ với User (admin upload)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User uploadedBy;
    
    @Column(nullable = false)
    private Boolean active = true;
}
