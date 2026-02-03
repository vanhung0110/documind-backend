package com.documindai.service;

import com.documindai.dto.response.DocumentResponse;
import com.documindai.model.Document;
import com.documindai.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface cho document management
 */
public interface DocumentService {

    /**
     * Upload và xử lý document
     */
    DocumentResponse uploadDocument(MultipartFile file, User admin);

    /**
     * Lấy danh sách tất cả documents
     */
    List<DocumentResponse> getAllDocuments();

    /**
     * Lấy danh sách documents của một admin
     */
    List<DocumentResponse> getDocumentsByAdmin(Long adminId);

    /**
     * Lấy document theo ID
     */
    DocumentResponse getDocumentById(Long id);

    /**
     * Xóa document
     */
    void deleteDocument(Long id);

    /**
     * Lấy document entity theo ID
     */
    Document getDocumentEntityById(Long id);

    /**
     * Đếm tổng số documents
     */
    long countDocuments();

    /**
     * Đếm số documents đã xử lý
     */
    long countProcessedDocuments();

    /**
     * Lấy nội dung text của document
     */
    String getDocumentContent(Long id);

    /**
     * Reprocess document (regenerate chunks and embeddings)
     */
    DocumentResponse reprocessDocument(Long id);
}
