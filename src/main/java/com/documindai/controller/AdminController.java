package com.documindai.controller;

import com.documindai.dto.response.ApiResponse;
import com.documindai.dto.response.DocumentResponse;
import com.documindai.dto.response.UserResponse;
import com.documindai.model.User;
import com.documindai.service.DocumentService;
import com.documindai.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller cho admin operations
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DocumentService documentService;
    private final UserService userService;

    /**
     * Upload document
     */
    @PostMapping("/documents/upload")
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Admin {} uploading document: {}", userDetails.getUsername(), file.getOriginalFilename());

        User admin = userService.getUserByUsername(userDetails.getUsername());
        DocumentResponse response = documentService.uploadDocument(file, admin);

        return ResponseEntity.ok(ApiResponse.success(response, "Document uploaded successfully"));
    }

    /**
     * Get all documents
     */
    @GetMapping("/documents")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getAllDocuments() {
        log.info("Admin fetching all documents");

        List<DocumentResponse> documents = documentService.getAllDocuments();

        return ResponseEntity.ok(ApiResponse.success(documents, "Documents retrieved successfully"));
    }

    /**
     * Get document by ID
     */
    @GetMapping("/documents/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocument(@PathVariable Long id) {
        log.info("Admin fetching document ID: {}", id);

        DocumentResponse document = documentService.getDocumentById(id);

        return ResponseEntity.ok(ApiResponse.success(document, "Document retrieved successfully"));
    }

    /**
     * Delete document
     */
    @DeleteMapping("/documents/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable Long id) {
        log.info("Admin deleting document ID: {}", id);

        documentService.deleteDocument(id);

        return ResponseEntity.ok(ApiResponse.success(null, "Document deleted successfully"));
    }

    /**
     * Reprocess document (regenerate chunks and embeddings)
     */
    @PostMapping("/documents/{id}/reprocess")
    public ResponseEntity<ApiResponse<DocumentResponse>> reprocessDocument(@PathVariable Long id) {
        log.info("Admin reprocessing document ID: {}", id);

        DocumentResponse document = documentService.reprocessDocument(id);

        return ResponseEntity.ok(ApiResponse.success(document, "Document reprocessed successfully"));
    }

    /**
     * Get all users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        log.info("Admin fetching all users");

        List<UserResponse> users = userService.getAllUsers();

        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    /**
     * Get system statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        log.info("Admin fetching system statistics");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.countUsers());
        stats.put("totalAdmins", userService.countUsersByRole("ROLE_ADMIN"));
        stats.put("totalDocuments", documentService.countDocuments());
        stats.put("processedDocuments", documentService.countProcessedDocuments());

        return ResponseEntity.ok(ApiResponse.success(stats, "Statistics retrieved successfully"));
    }
}
