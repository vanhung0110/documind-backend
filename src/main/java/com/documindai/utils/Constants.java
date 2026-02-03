package com.documindai.utils;

/**
 * Class chứa các hằng số sử dụng trong toàn bộ ứng dụng
 */
public class Constants {
    
    // JWT Constants
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    
    // File Upload Constants
    public static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    public static final String[] ALLOWED_EXTENSIONS = {"pdf", "doc", "docx", "txt"};
    
    // OpenAI Constants
    public static final String OPENAI_SYSTEM_PROMPT = 
        "Bạn là một trợ lý AI thông minh có khả năng trả lời câu hỏi dựa trên " +
        "các tài liệu đã được cung cấp. Hãy trả lời chính xác, ngắn gọn và " +
        "dễ hiểu. Nếu không tìm thấy thông tin trong tài liệu, hãy thông báo rõ ràng.";
    
    public static final int DEFAULT_MAX_TOKENS = 2000;
    public static final double DEFAULT_TEMPERATURE = 0.7;
    public static final int CONTEXT_WINDOW_SIZE = 10; // Số messages gần nhất để lấy context
    
    // Document Processing Constants
    public static final int DOCUMENT_CHUNK_SIZE = 1000; // Kích thước mỗi chunk text
    public static final int DOCUMENT_CHUNK_OVERLAP = 200; // Overlap giữa các chunks
    
    // Pagination Constants
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    
    // Date Format
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    
    // Response Messages
    public static final String SUCCESS = "Thành công";
    public static final String ERROR = "Đã xảy ra lỗi";
    public static final String UNAUTHORIZED = "Không có quyền truy cập";
    public static final String NOT_FOUND = "Không tìm thấy";
    public static final String BAD_REQUEST = "Yêu cầu không hợp lệ";
    
    // Roles
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";
    
    private Constants() {
        // Private constructor để prevent instantiation
    }
}
