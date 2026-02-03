package com.documindai.service;

import com.documindai.model.Message;

import java.util.List;

/**
 * Service interface cho OpenAI integration
 */
public interface OpenAIService {

    /**
     * Tạo embedding cho text
     */
    List<Double> createEmbedding(String text);

    /**
     * Chat completion với context từ documents
     */
    String chatWithContext(String userMessage, List<String> contextChunks, List<Message> conversationHistory);

    /**
     * Tạo summary cho document
     */
    String summarizeDocument(String documentContent);

    /**
     * Chat completion đơn giản không có context
     */
    String simpleChat(String message);
}
