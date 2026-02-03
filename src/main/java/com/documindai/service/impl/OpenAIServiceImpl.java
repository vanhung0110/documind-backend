package com.documindai.service.impl;

import com.documindai.model.Message;
import com.documindai.service.OpenAIService;
import com.documindai.utils.PromptBuilder;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation của OpenAIService
 * Xử lý tất cả tương tác với OpenAI API
 */
@Service
@Slf4j
public class OpenAIServiceImpl implements OpenAIService {

    private final OpenAiService openAiService;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.embedding.model}")
    private String embeddingModel;

    @Value("${openai.max.tokens}")
    private int maxTokens;

    @Value("${openai.temperature}")
    private double temperature;

    @Value("${openai.top.p:0.95}")
    private double topP;

    @Value("${openai.frequency.penalty:0.3}")
    private double frequencyPenalty;

    @Value("${openai.presence.penalty:0.3}")
    private double presencePenalty;

    @Value("${openai.system.prompt}")
    private String systemPrompt;

    public OpenAIServiceImpl(@Value("${openai.api.key}") String apiKey) {
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
        log.info("OpenAI Service initialized with model: {}", model);
    }

    @Override
    public List<Double> createEmbedding(String text) {
        try {
            log.debug("Creating embedding for text of length: {}", text.length());

            EmbeddingRequest embeddingRequest = EmbeddingRequest.builder()
                    .model(embeddingModel)
                    .input(List.of(text))
                    .build();

            EmbeddingResult result = openAiService.createEmbeddings(embeddingRequest);

            if (result.getData() != null && !result.getData().isEmpty()) {
                List<Double> embedding = result.getData().get(0).getEmbedding();
                log.debug("Successfully created embedding with {} dimensions", embedding.size());
                return embedding;
            }

            log.error("No embedding data returned from OpenAI");
            return new ArrayList<>();

        } catch (Exception e) {
            log.error("Error creating embedding: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create embedding: " + e.getMessage(), e);
        }
    }

    @Override
    public String chatWithContext(String userMessage, List<String> contextChunks, List<Message> conversationHistory) {
        try {
            log.info("Chat with context - User message: {}, Context chunks: {}, History messages: {}",
                    userMessage, contextChunks.size(), conversationHistory != null ? conversationHistory.size() : 0);

            List<ChatMessage> messages = new ArrayList<>();

            // System message với context
            boolean hasContext = contextChunks != null && !contextChunks.isEmpty();
            String systemMessage = PromptBuilder.buildSystemMessage(systemPrompt, hasContext);
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage));

            // Thêm conversation history (giới hạn số lượng để tránh vượt quá token limit)
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                int historyLimit = Math.min(conversationHistory.size(), 10);
                List<Message> recentHistory = conversationHistory.subList(
                        Math.max(0, conversationHistory.size() - historyLimit),
                        conversationHistory.size());

                for (Message msg : recentHistory) {
                    String role = msg.getRole() == Message.MessageRole.USER ? ChatMessageRole.USER.value()
                            : ChatMessageRole.ASSISTANT.value();
                    messages.add(new ChatMessage(role, msg.getContent()));
                }
            }

            // Tạo user message với context
            String userPrompt;
            if (hasContext) {
                userPrompt = PromptBuilder.buildContextualPrompt(userMessage, contextChunks);
            } else {
                userPrompt = userMessage;
            }

            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userPrompt));

            // Tạo chat completion request
            ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .maxTokens(maxTokens)
                    .temperature(temperature)
                    .topP(topP)
                    .frequencyPenalty(frequencyPenalty)
                    .presencePenalty(presencePenalty)
                    .build();

            // Gọi OpenAI API
            ChatCompletionResult result = openAiService.createChatCompletion(chatRequest);

            if (result.getChoices() != null && !result.getChoices().isEmpty()) {
                String response = result.getChoices().get(0).getMessage().getContent();
                log.info("Received response from OpenAI, length: {}", response.length());
                return response;
            }

            log.error("No response from OpenAI");
            return "Xin lỗi, tôi không thể tạo câu trả lời lúc này. Vui lòng thử lại.";

        } catch (Exception e) {
            log.error("Error in chat with context: {}", e.getMessage(), e);
            return "Đã xảy ra lỗi khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.";
        }
    }

    @Override
    public String summarizeDocument(String documentContent) {
        try {
            log.info("Summarizing document of length: {}", documentContent.length());

            // Giới hạn độ dài content để tránh vượt quá token limit
            String contentToSummarize = documentContent;
            if (documentContent.length() > 10000) {
                contentToSummarize = documentContent.substring(0, 10000);
                log.info("Truncated document content to 10000 characters for summarization");
            }

            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(),
                    "Bạn là trợ lý AI chuyên tóm tắt tài liệu. Hãy tóm tắt nội dung một cách ngắn gọn, súc tích và dễ hiểu."));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(),
                    PromptBuilder.buildSummarizationPrompt(contentToSummarize)));

            ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .maxTokens(500)
                    .temperature(0.5)
                    .build();

            ChatCompletionResult result = openAiService.createChatCompletion(chatRequest);

            if (result.getChoices() != null && !result.getChoices().isEmpty()) {
                String summary = result.getChoices().get(0).getMessage().getContent();
                log.info("Successfully created summary");
                return summary;
            }

            return "Không thể tạo tóm tắt cho tài liệu này.";

        } catch (Exception e) {
            log.error("Error summarizing document: {}", e.getMessage(), e);
            return "Lỗi khi tạo tóm tắt tài liệu.";
        }
    }

    @Override
    public String simpleChat(String message) {
        try {
            log.info("Simple chat - Message: {}", message);

            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), message));

            ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .maxTokens(maxTokens)
                    .temperature(temperature)
                    .build();

            ChatCompletionResult result = openAiService.createChatCompletion(chatRequest);

            if (result.getChoices() != null && !result.getChoices().isEmpty()) {
                return result.getChoices().get(0).getMessage().getContent();
            }

            return "Xin lỗi, tôi không thể trả lời lúc này.";

        } catch (Exception e) {
            log.error("Error in simple chat: {}", e.getMessage(), e);
            return "Đã xảy ra lỗi. Vui lòng thử lại.";
        }
    }
}
