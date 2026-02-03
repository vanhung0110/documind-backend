package com.documindai.utils;

import com.documindai.model.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class để xây dựng prompts cho OpenAI
 */
@Slf4j
public class PromptBuilder {

    /**
     * Xây dựng prompt với context từ documents
     */
    public static String buildContextualPrompt(String userQuestion, List<String> relevantChunks) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Dựa trên các thông tin sau đây từ tài liệu:\n\n");

        for (int i = 0; i < relevantChunks.size(); i++) {
            prompt.append("--- Đoạn ").append(i + 1).append(" ---\n");
            prompt.append(relevantChunks.get(i)).append("\n\n");
        }

        prompt.append("Hãy trả lời câu hỏi sau một cách tự nhiên, thân thiện và chi tiết:\n");
        prompt.append(userQuestion);

        return prompt.toString();
    }

    /**
     * Xây dựng prompt khi không tìm thấy context phù hợp
     */
    public static String buildNoContextPrompt(String userQuestion) {
        return "Tôi không tìm thấy thông tin liên quan trong tài liệu để trả lời câu hỏi: \""
                + userQuestion + "\". Bạn có thể hỏi về nội dung khác có trong tài liệu không?";
    }

    /**
     * Xây dựng conversation history cho context
     */
    public static String buildConversationHistory(List<Message> recentMessages) {
        if (recentMessages == null || recentMessages.isEmpty()) {
            return "";
        }

        return recentMessages.stream()
                .map(msg -> {
                    String role = msg.getRole() == Message.MessageRole.USER ? "Người dùng" : "Trợ lý";
                    return role + ": " + msg.getContent();
                })
                .collect(Collectors.joining("\n"));
    }

    /**
     * Tạo system message cho OpenAI
     */
    public static String buildSystemMessage(String baseSystemPrompt, boolean hasContext) {
        StringBuilder systemMsg = new StringBuilder(baseSystemPrompt);

        if (hasContext) {
            systemMsg.append("\n\n[[CONTEXT MODE ACTIVE]]\n");
            systemMsg.append("Bạn đang có quyền truy cập vào các đoạn văn bản (chunks) từ tài liệu của người dùng. ");
            systemMsg.append(
                    "Hãy sử dụng thông tin này để trả lời câu hỏi. Ghi nhớ các quy tắc: Trung thực, Dẫn chứng, và Không từ chối nếu có dữ liệu.");
        } else {
            systemMsg.append("\n\n[[NO CONTEXT]]\n");
            systemMsg.append("Hiện tại không tìm thấy thông tin phù hợp trong Knowledge Base cho câu hỏi này. ");
            systemMsg.append(
                    "Hãy thông báo cho người dùng biết rằng bạn chỉ có thể trả lời dựa trên tài liệu đã tải lên.");
        }

        return systemMsg.toString();
    }

    /**
     * Tạo prompt để tóm tắt document
     */
    public static String buildSummarizationPrompt(String documentContent) {
        return "Hãy tóm tắt nội dung chính của tài liệu sau đây một cách ngắn gọn và súc tích:\n\n"
                + documentContent;
    }
}
