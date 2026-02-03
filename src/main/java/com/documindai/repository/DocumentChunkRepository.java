package com.documindai.repository;

import com.documindai.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho DocumentChunk entity
 */
@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    /**
     * Tìm tất cả chunks của một document
     */
    List<DocumentChunk> findByDocumentIdOrderByChunkIndexAsc(Long documentId);

    /**
     * Tìm tất cả chunks có embedding (đã được xử lý)
     */
    @Query("SELECT dc FROM DocumentChunk dc WHERE dc.embedding IS NOT NULL AND dc.document.active = true")
    List<DocumentChunk> findAllWithEmbeddings();

    /**
     * Xóa tất cả chunks của một document
     */
    void deleteByDocumentId(Long documentId);

    /**
     * Đếm số chunks của một document
     */
    long countByDocumentId(Long documentId);

    /**
     * Tìm chunks chưa có embedding
     */
    @Query("SELECT dc FROM DocumentChunk dc WHERE dc.embedding IS NULL")
    List<DocumentChunk> findChunksWithoutEmbeddings();
}
