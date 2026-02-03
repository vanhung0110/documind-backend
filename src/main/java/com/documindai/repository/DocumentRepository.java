package com.documindai.repository;

import com.documindai.model.Document;
import com.documindai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho Document entity
 * Quản lý các tài liệu được upload vào hệ thống
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

       /**
        * Tìm tất cả documents đang active
        */
       List<Document> findByActiveTrue();

       /**
        * Tìm documents theo admin đã upload
        */
       List<Document> findByUploadedBy(User admin);

       /**
        * Tìm documents theo admin ID
        */
       List<Document> findByUploadedById(Long adminId);

       /**
        * Tìm documents đã được xử lý (processed = true)
        */
       List<Document> findByProcessedTrue();

       /**
        * Tìm documents chưa được xử lý
        */
       List<Document> findByProcessedFalse();

       /**
        * Tìm documents theo file type
        */
       List<Document> findByFileType(String fileType);

       /**
        * Tìm documents theo original filename
        */
       Optional<Document> findByOriginalFilename(String originalFilename);

       /**
        * Tìm documents được upload trong khoảng thời gian
        */
       List<Document> findByUploadDateBetween(LocalDateTime startDate, LocalDateTime endDate);

       /**
        * Tìm kiếm documents theo filename hoặc nội dung
        */
       @Query("SELECT d FROM Document d WHERE d.active = true AND " +
                     "(d.originalFilename LIKE %:keyword% OR d.extractedContent LIKE %:keyword%)")
       List<Document> searchDocuments(@Param("keyword") String keyword);

       /**
        * Lấy tất cả documents đã processed để sử dụng cho AI context
        */
       @Query("SELECT d FROM Document d WHERE d.active = true AND d.processed = true " +
                     "ORDER BY d.uploadDate DESC")
       List<Document> findAllProcessedDocuments();

       /**
        * Đếm số lượng documents theo admin
        */
       Long countByUploadedById(Long adminId);

       /**
        * Tính tổng dung lượng files đã upload
        */
       @Query("SELECT SUM(d.fileSize) FROM Document d WHERE d.active = true")
       Long getTotalFileSize();

       /**
        * Lấy documents mới nhất (giới hạn số lượng)
        */
       List<Document> findTop10ByActiveTrueOrderByUploadDateDesc();

       /**
        * Tìm documents theo active status, sắp xếp theo ngày upload
        */
       List<Document> findByActiveOrderByUploadDateDesc(boolean active);

       /**
        * Tìm documents theo admin ID và active status
        */
       List<Document> findByUploadedByIdAndActiveOrderByUploadDateDesc(Long adminId, boolean active);

       /**
        * Đếm số documents theo active status
        */
       long countByActive(boolean active);

       /**
        * Đếm số documents đã xử lý và active
        */
       long countByProcessedAndActive(boolean processed, boolean active);
}
