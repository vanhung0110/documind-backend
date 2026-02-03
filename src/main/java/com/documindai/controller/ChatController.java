package com.documindai.controller;

import com.documindai.dto.request.ChatRequest;
import com.documindai.dto.response.ApiResponse;
import com.documindai.dto.response.ChatResponse;
import com.documindai.dto.response.ChatSessionResponse;
import com.documindai.model.User;
import com.documindai.service.ChatService;
import com.documindai.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cho chat endpoints
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final com.documindai.service.DocumentService documentService;

    /**
     * Send chat message
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ChatResponse>> sendMessage(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("User {} sending chat message", userDetails.getUsername());

        User user = userService.getUserByUsername(userDetails.getUsername());
        ChatResponse response = chatService.sendMessage(request, user);

        return ResponseEntity.ok(ApiResponse.success(response, "Message sent successfully"));
    }

    /**
     * Create new chat session
     */
    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<ChatSessionResponse>> createSession(
            @RequestParam(required = false) String title,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("User {} creating new chat session", userDetails.getUsername());

        User user = userService.getUserByUsername(userDetails.getUsername());
        ChatSessionResponse response = chatService.createSession(user, title);

        return ResponseEntity.ok(ApiResponse.success(response, "Session created successfully"));
    }

    /**
     * Get all user's chat sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<ChatSessionResponse>>> getUserSessions(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("User {} fetching chat sessions", userDetails.getUsername());

        User user = userService.getUserByUsername(userDetails.getUsername());
        List<ChatSessionResponse> sessions = chatService.getUserSessions(user);

        return ResponseEntity.ok(ApiResponse.success(sessions, "Sessions retrieved successfully"));
    }

    /**
     * Get session by ID
     */
    @GetMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<ChatSessionResponse>> getSession(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("User {} fetching session ID: {}", userDetails.getUsername(), id);

        User user = userService.getUserByUsername(userDetails.getUsername());
        ChatSessionResponse session = chatService.getSessionById(id, user);

        return ResponseEntity.ok(ApiResponse.success(session, "Session retrieved successfully"));
    }

    /**
     * Get session chat history
     */
    @GetMapping("/sessions/{id}/history")
    public ResponseEntity<ApiResponse<List<ChatResponse>>> getSessionHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("User {} fetching history for session ID: {}", userDetails.getUsername(), id);

        User user = userService.getUserByUsername(userDetails.getUsername());
        List<ChatResponse> history = chatService.getSessionHistory(id, user);

        return ResponseEntity.ok(ApiResponse.success(history, "History retrieved successfully"));
    }

    /**
     * Delete chat session
     */
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("User {} deleting session ID: {}", userDetails.getUsername(), id);

        User user = userService.getUserByUsername(userDetails.getUsername());
        chatService.deleteSession(id, user);

        return ResponseEntity.ok(ApiResponse.success(null, "Session deleted successfully"));
    }

    /**
     * Get document content for preview
     */
    @GetMapping("/documents/{id}/preview")
    public ResponseEntity<ApiResponse<String>> getDocumentPreview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("User {} fetching preview for document ID: {}", userDetails.getUsername(), id);

        String content = documentService.getDocumentContent(id);

        return ResponseEntity.ok(ApiResponse.success(content, "Document preview retrieved successfully"));
    }
}
