package com.documindai.service.impl;

import com.documindai.dto.request.ChatRequest;
import com.documindai.dto.response.ChatResponse;
import com.documindai.dto.response.ChatSessionResponse;
import com.documindai.exception.BadRequestException;
import com.documindai.exception.ResourceNotFoundException;
import com.documindai.model.ChatSession;
import com.documindai.model.DocumentChunk;
import com.documindai.model.Message;
import com.documindai.model.User;
import com.documindai.repository.ChatSessionRepository;
import com.documindai.repository.MessageRepository;
import com.documindai.service.ChatService;
import com.documindai.service.DocumentChunkingService;
import com.documindai.service.OpenAIService;
import com.documindai.utils.EmbeddingUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của ChatService với RAG (Retrieval Augmented Generation)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatSessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final OpenAIService openAIService;
    private final DocumentChunkingService chunkingService;
    private final ObjectMapper objectMapper;

    @Value("${app.max.context.chunks:5}")
    private int maxContextChunks;

    @Value("${app.similarity.threshold:0.7}")
    private double similarityThreshold;

    @Value("${app.conversation.context.messages:10}")
    private int conversationContextMessages;

    @Override
    @Transactional
    public ChatResponse sendMessage(ChatRequest request, User user) {
        log.info("Processing chat message from user: {}", user.getUsername());

        // Validate request
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new BadRequestException("Message cannot be empty");
        }

        // Lấy hoặc tạo chat session
        ChatSession session;
        if (request.getSessionId() != null) {
            session = sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));

            if (!session.getUser().getId().equals(user.getId())) {
                throw new BadRequestException("You don't have permission to access this session");
            }
        } else {
            // Tạo session mới
            session = new ChatSession();
            session.setUser(user);
            session.setTitle(generateSessionTitle(request.getMessage()));
            session.setActive(true);
            session = sessionRepository.save(session);
            log.info("Created new chat session with ID: {}", session.getId());
        }

        // Lưu user message
        Message userMessage = new Message();
        userMessage.setChatSession(session);
        userMessage.setRole(Message.MessageRole.USER);
        userMessage.setContent(request.getMessage());
        userMessage = messageRepository.save(userMessage);

        // Tạo embedding cho user query
        List<Double> queryEmbedding = openAIService.createEmbedding(request.getMessage());
        String queryEmbeddingJson = EmbeddingUtils.serializeEmbedding(queryEmbedding);

        // Tìm relevant document chunks
        List<DocumentChunk> relevantChunks = chunkingService.findSimilarChunks(
                queryEmbeddingJson,
                maxContextChunks,
                similarityThreshold);

        log.info("Found {} relevant chunks for query", relevantChunks.size());

        // Lấy conversation history
        List<Message> conversationHistory = messageRepository
                .findByChatSessionIdOrderByTimestampAsc(session.getId());

        // Giới hạn số lượng messages trong history
        if (conversationHistory.size() > conversationContextMessages) {
            conversationHistory = conversationHistory.subList(
                    conversationHistory.size() - conversationContextMessages,
                    conversationHistory.size());
        }

        // Tạo context từ relevant chunks
        List<String> contextChunks = relevantChunks.stream()
                .map(DocumentChunk::getContent)
                .collect(Collectors.toList());

        // Gọi OpenAI để tạo response
        String aiResponse = openAIService.chatWithContext(
                request.getMessage(),
                contextChunks,
                conversationHistory);

        // Lưu AI response
        Message assistantMessage = new Message();
        assistantMessage.setChatSession(session);
        assistantMessage.setRole(Message.MessageRole.ASSISTANT);
        assistantMessage.setContent(aiResponse);

        // Lưu context và source documents
        if (!relevantChunks.isEmpty()) {
            assistantMessage.setContext(String.join("\n---\n", contextChunks));

            List<Long> sourceDocIds = relevantChunks.stream()
                    .map(chunk -> chunk.getDocument().getId())
                    .distinct()
                    .collect(Collectors.toList());

            try {
                assistantMessage.setSourceDocuments(objectMapper.writeValueAsString(sourceDocIds));
            } catch (Exception e) {
                log.error("Error serializing source documents", e);
            }

            // Tính confidence score dựa trên similarity
            double avgSimilarity = relevantChunks.stream()
                    .mapToDouble(chunk -> {
                        return EmbeddingUtils.calculateSimilarity(queryEmbeddingJson, chunk.getEmbedding());
                    })
                    .average()
                    .orElse(0.0);

            assistantMessage.setConfidenceScore(avgSimilarity);
        } else {
            assistantMessage.setConfidenceScore(0.0);
        }

        assistantMessage = messageRepository.save(assistantMessage);

        // Update session last message time
        session.setLastMessageAt(LocalDateTime.now());
        sessionRepository.save(session);

        // Tạo response
        ChatResponse response = new ChatResponse();
        response.setSessionId(session.getId());
        response.setMessage(aiResponse);
        response.setTimestamp(assistantMessage.getTimestamp());
        response.setConfidenceScore(assistantMessage.getConfidenceScore());

        if (!relevantChunks.isEmpty()) {
            List<String> sourceDocNames = relevantChunks.stream()
                    .map(chunk -> chunk.getDocument().getOriginalFilename())
                    .distinct()
                    .collect(Collectors.toList());
            response.setSourceDocuments(sourceDocNames);
        }

        log.info("Chat response generated successfully");

        return response;
    }

    @Override
    @Transactional
    public ChatSessionResponse createSession(User user, String title) {
        log.info("Creating new chat session for user: {}", user.getUsername());

        ChatSession session = new ChatSession();
        session.setUser(user);
        session.setTitle(title != null && !title.isEmpty() ? title : "New Conversation");
        session.setActive(true);

        session = sessionRepository.save(session);

        return mapToSessionResponse(session);
    }

    @Override
    public List<ChatSessionResponse> getUserSessions(User user) {
        List<ChatSession> sessions = sessionRepository
                .findByUserIdAndActiveOrderByLastMessageAtDesc(user.getId(), true);

        return sessions.stream()
                .map(this::mapToSessionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ChatSessionResponse getSessionById(Long sessionId, User user) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to access this session");
        }

        return mapToSessionResponse(session);
    }

    @Override
    @Transactional
    public void deleteSession(Long sessionId, User user) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to delete this session");
        }

        session.setActive(false);
        sessionRepository.save(session);

        log.info("Deleted chat session ID: {}", sessionId);
    }

    @Override
    public List<ChatResponse> getSessionHistory(Long sessionId, User user) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to access this session");
        }

        List<Message> messages = messageRepository.findByChatSessionIdOrderByTimestampAsc(sessionId);

        return messages.stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }

    /**
     * Generate session title từ first message
     */
    private String generateSessionTitle(String firstMessage) {
        if (firstMessage.length() > 50) {
            return firstMessage.substring(0, 47) + "...";
        }
        return firstMessage;
    }

    /**
     * Map ChatSession to ChatSessionResponse
     */
    private ChatSessionResponse mapToSessionResponse(ChatSession session) {
        ChatSessionResponse response = new ChatSessionResponse();
        response.setId(session.getId());
        response.setTitle(session.getTitle());
        response.setCreatedAt(session.getCreatedAt());
        response.setLastMessageTime(session.getLastMessageAt());

        // Đếm số messages
        long messageCount = messageRepository.countByChatSessionId(session.getId());
        response.setMessageCount((int) messageCount);

        return response;
    }

    /**
     * Map Message to ChatResponse
     */
    private ChatResponse mapToMessageResponse(Message message) {
        ChatResponse response = new ChatResponse();
        response.setSessionId(message.getChatSession().getId());
        response.setMessage(message.getContent());
        response.setRole(message.getRole().toString());
        response.setTimestamp(message.getTimestamp());
        response.setConfidenceScore(message.getConfidenceScore());

        // Parse source documents
        if (message.getSourceDocuments() != null) {
            try {
                // Parse document IDs (could be used to load document names if needed)
                objectMapper.readValue(
                        message.getSourceDocuments(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class));
                response.setSourceDocuments(new ArrayList<>());
            } catch (Exception e) {
                log.error("Error parsing source documents", e);
            }
        }

        return response;
    }
}
