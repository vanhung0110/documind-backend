package com.documindai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho các chunk (đoạn) của tài liệu
 * Mỗi tài liệu được chia thành nhiều chunk để xử lý embedding hiệu quả
 */
@Entity
@Table(name = "document_chunks", indexes = {
        @Index(name = "idx_document_id", columnList = "document_id"),
        @Index(name = "idx_chunk_index", columnList = "chunk_index")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false)
    private Integer chunkIndex; // Thứ tự chunk trong document

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // Nội dung text của chunk

    @Column(columnDefinition = "LONGTEXT")
    private String embedding; // Vector embedding (JSON format)

    @Column(nullable = false)
    private Integer startPosition; // Vị trí bắt đầu trong document gốc

    @Column(nullable = false)
    private Integer endPosition; // Vị trí kết thúc trong document gốc

    @Column(nullable = false)
    private Integer tokenCount = 0; // Số token trong chunk

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
