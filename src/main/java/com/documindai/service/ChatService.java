package com.documindai.service;

import com.documindai.dto.request.ChatRequest;
import com.documindai.dto.response.ChatResponse;
import com.documindai.dto.response.ChatSessionResponse;
import com.documindai.model.User;

import java.util.List;

/**
 * Service interface cho chat functionality
 */
public interface ChatService {

    /**
     * Gửi message và nhận response từ AI
     */
    ChatResponse sendMessage(ChatRequest request, User user);

    /**
     * Tạo chat session mới
     */
    ChatSessionResponse createSession(User user, String title);

    /**
     * Lấy tất cả sessions của user
     */
    List<ChatSessionResponse> getUserSessions(User user);

    /**
     * Lấy session theo ID
     */
    ChatSessionResponse getSessionById(Long sessionId, User user);

    /**
     * Xóa session
     */
    void deleteSession(Long sessionId, User user);

    /**
     * Lấy lịch sử chat của một session
     */
    List<ChatResponse> getSessionHistory(Long sessionId, User user);
}
