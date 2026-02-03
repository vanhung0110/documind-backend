package com.documindai.service.impl;

import com.documindai.dto.response.DocumentResponse;
import com.documindai.exception.BadRequestException;
import com.documindai.exception.FileStorageException;
import com.documindai.exception.ResourceNotFoundException;
import com.documindai.model.Document;
import com.documindai.model.DocumentChunk;
import com.documindai.model.User;
import com.documindai.repository.DocumentRepository;
import com.documindai.service.DocumentChunkingService;
import com.documindai.service.DocumentService;
import com.documindai.service.OpenAIService;
import com.documindai.utils.DocumentProcessor;
import com.documindai.utils.EmbeddingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation của DocumentService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentProcessor documentProcessor;
    private final DocumentChunkingService chunkingService;
    private final OpenAIService openAIService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.allowed-extensions}")
    private String allowedExtensions;

    @Override
    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file, User admin) {
        log.info("Uploading document: {} by admin: {}", file.getOriginalFilename(), admin.getUsername());

        // Validate file
        validateFile(file);

        try {
            // Tạo thư mục upload nếu chưa tồn tại
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Tạo tên file unique
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = getFileExtension(originalFilename);
            String filename = UUID.randomUUID().toString() + "." + extension;

            // Lưu file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File saved to: {}", filePath.toString());

            // Tạo Document entity
            Document document = new Document();
            document.setFilename(filename);
            document.setOriginalFilename(originalFilename);
            document.setFilePath(filePath.toString());
            document.setFileType(extension);
            document.setFileSize(file.getSize());
            document.setUploadedBy(admin);
            document.setActive(true);
            document.setProcessed(false);

            // Lưu document vào database
            document = documentRepository.save(document);
            log.info("Document saved to database with ID: {}", document.getId());

            // Xử lý document bất đồng bộ (extract text, chunk, embedding)
            processDocumentAsync(document, filePath.toFile());

            return mapToResponse(document);

        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            throw new FileStorageException("Could not store file: " + e.getMessage());
        }
    }

    /**
     * Xử lý document: extract text, chunk, và tạo embeddings
     */
    private void processDocumentAsync(Document document, File file) {
        // Trong production, nên sử dụng @Async hoặc message queue
        // Ở đây xử lý đồng bộ để đơn giản

        try {
            log.info("Processing document ID: {}", document.getId());

            // 1. Extract text từ file
            String extractedText = documentProcessor.extractText(file, document.getFileType());
            document.setExtractedContent(extractedText);

            log.info("Extracted {} characters from document", extractedText.length());

            // 2. Tạo summary
            String summary = openAIService.summarizeDocument(extractedText);
            document.setSummary(summary);

            log.info("Created summary for document");

            // 3. Chia thành chunks
            List<DocumentChunk> chunks = chunkingService.chunkDocument(document, extractedText);

            log.info("Created {} chunks for document", chunks.size());

            // 4. Tạo embeddings cho mỗi chunk
            for (DocumentChunk chunk : chunks) {
                try {
                    List<Double> embedding = openAIService.createEmbedding(chunk.getContent());
                    String embeddingJson = EmbeddingUtils.serializeEmbedding(embedding);
                    chunk.setEmbedding(embeddingJson);
                } catch (Exception e) {
                    log.error("Error creating embedding for chunk {}: {}", chunk.getId(), e.getMessage());
                }
            }
            
            // Save chunks with embeddings
            chunkingService.saveChunks(chunks);
            log.info("Saved embeddings for {} chunks", chunks.size());

            // 5. Tạo embedding cho toàn bộ document (hoặc summary)
            try {
                List<Double> docEmbedding = openAIService.createEmbedding(summary);
                String embeddingJson = EmbeddingUtils.serializeEmbedding(docEmbedding);
                document.setEmbedding(embeddingJson);
            } catch (Exception e) {
                log.error("Error creating document embedding: {}", e.getMessage());
            }

            // 6. Đánh dấu là đã xử lý
            document.setProcessed(true);
            documentRepository.save(document);

            log.info("Document processing completed for ID: {}", document.getId());

        } catch (Exception e) {
            log.error("Error processing document {}: {}", document.getId(), e.getMessage(), e);
            document.setProcessed(false);
            documentRepository.save(document);
        }
    }

    @Override
    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findByActiveOrderByUploadDateDesc(true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentResponse> getDocumentsByAdmin(Long adminId) {
        return documentRepository.findByUploadedByIdAndActiveOrderByUploadDateDesc(adminId, true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentResponse getDocumentById(Long id) {
        Document document = getDocumentEntityById(id);
        return mapToResponse(document);
    }

    @Override
    public Document getDocumentEntityById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        log.info("Deleting document ID: {}", id);

        Document document = getDocumentEntityById(id);

        // Xóa file vật lý
        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
            log.info("Deleted physical file: {}", filePath);
        } catch (IOException e) {
            log.error("Error deleting physical file: {}", e.getMessage());
        }

        // Xóa chunks
        chunkingService.deleteDocumentChunks(id);

        // Soft delete document
        document.setActive(false);
        documentRepository.save(document);

        log.info("Document deleted successfully");
    }

    @Override
    public long countDocuments() {
        return documentRepository.countByActive(true);
    }

    @Override
    public long countProcessedDocuments() {
        return documentRepository.countByProcessedAndActive(true, true);
    }

    @Override
    public String getDocumentContent(Long id) {
        Document document = getDocumentEntityById(id);
        return document.getExtractedContent();
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BadRequestException("Invalid filename");
        }

        String extension = getFileExtension(originalFilename);
        List<String> allowed = Arrays.asList(allowedExtensions.split(","));

        if (!allowed.contains(extension.toLowerCase())) {
            throw new BadRequestException("File type not allowed. Allowed types: " + allowedExtensions);
        }

        // Giới hạn kích thước file (50MB)
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new BadRequestException("File size exceeds maximum limit of 50MB");
        }
    }

    /**
     * Lấy file extension
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * Map Document entity to DocumentResponse DTO
     */
    private DocumentResponse mapToResponse(Document document) {
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setFilename(document.getOriginalFilename());
        response.setFileType(document.getFileType());
        response.setFileSize(document.getFileSize());
        response.setSummary(document.getSummary());
        response.setProcessed(document.getProcessed());
        response.setUploadDate(document.getUploadDate());
        response.setUploadedBy(document.getUploadedBy().getUsername());
        return response;
    }

    @Override
    @Transactional
    public DocumentResponse reprocessDocument(Long id) {
        log.info("Reprocessing document ID: {}", id);
        
        Document document = getDocumentEntityById(id);
        
        // Delete existing chunks
        chunkingService.deleteDocumentChunks(id);
        
        // Reprocess
        File file = new File(document.getFilePath());
        if (!file.exists()) {
            throw new ResourceNotFoundException("Document file not found on disk");
        }
        
        try {
            // 1. Extract text
            String extractedText = document.getExtractedContent();
            if (extractedText == null || extractedText.isEmpty()) {
                extractedText = documentProcessor.extractText(file, document.getFileType());
                document.setExtractedContent(extractedText);
            }
            
            // 2. Create chunks
            List<DocumentChunk> chunks = chunkingService.chunkDocument(document, extractedText);
            log.info("Created {} chunks for document", chunks.size());
            
            // 3. Create embeddings for each chunk
            int embeddedCount = 0;
            for (DocumentChunk chunk : chunks) {
                try {
                    List<Double> embedding = openAIService.createEmbedding(chunk.getContent());
                    String embeddingJson = EmbeddingUtils.serializeEmbedding(embedding);
                    chunk.setEmbedding(embeddingJson);
                    embeddedCount++;
                } catch (Exception e) {
                    log.error("Error creating embedding for chunk {}: {}", chunk.getId(), e.getMessage());
                }
            }
            
            // 4. Save chunks with embeddings
            chunkingService.saveChunks(chunks);
            log.info("Saved embeddings for {} chunks", embeddedCount);
            
            // 5. Mark as processed
            document.setProcessed(true);
            documentRepository.save(document);
            
            log.info("Document reprocessing completed for ID: {}", id);
            
            return mapToResponse(document);
            
        } catch (Exception e) {
            log.error("Error reprocessing document {}: {}", id, e.getMessage(), e);
            throw new BadRequestException("Failed to reprocess document: " + e.getMessage());
        }
    }
}
