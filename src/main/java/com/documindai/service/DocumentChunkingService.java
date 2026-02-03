package com.documindai.service;

import com.documindai.model.Document;
import com.documindai.model.DocumentChunk;

import java.util.List;

/**
 * Service interface cho document chunking
 */
public interface DocumentChunkingService {

    /**
     * Chia document thành các chunks và lưu vào database
     */
    List<DocumentChunk> chunkDocument(Document document, String extractedText);

    /**
     * Lấy tất cả chunks của một document
     */
    List<DocumentChunk> getDocumentChunks(Long documentId);

    /**
     * Xóa tất cả chunks của một document
     */
    void deleteDocumentChunks(Long documentId);

    /**
     * Lưu danh sách chunks (sau khi cập nhật embeddings)
     */
    List<DocumentChunk> saveChunks(List<DocumentChunk> chunks);

    /**
     * Tìm các chunks có nội dung tương tự với query
     */
    List<DocumentChunk> findSimilarChunks(String queryEmbedding, int limit, double threshold);
}
