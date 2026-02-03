package com.documindai;

import com.documindai.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.modelmapper.ModelMapper;



/**
 * DocumindAI Application - Main Entry Point
 * 
 * Ứng dụng chatbot AI có khả năng học từ tài liệu và trả lời câu hỏi
 * dựa trên nội dung đã được upload bởi Admin.
 * 
 * @author DocumindAI Team
 * @version 1.0.0
 */
@SpringBootApplication
@Slf4j
public class DocumindAiApplication {

    public static void main(String[] args) {
        // Load .env file
       

        SpringApplication.run(DocumindAiApplication.class, args);
        System.out.println("\n" +
                "╔══════════════════════════════════════════════════════════╗\n" +
                "║                                                          ║\n" +
                "║              DOCUMINDAI BACKEND STARTED                  ║\n" +
                "║                                                          ║\n" +
                "║  🚀 Server: http://localhost:8080/api                   ║\n" +
                "║  📚 Swagger: http://localhost:8080/api/swagger-ui.html  ║\n" +
                "║  💾 Database: MySQL (documindai_db)                     ║\n" +
                "║  🤖 AI: OpenAI GPT Integration                          ║\n" +
                "║                                                          ║\n" +
                "╚══════════════════════════════════════════════════════════╝\n");
    }

    /**
     * Bean cho mã hóa password sử dụng BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean cho mapping giữa Entity và DTO
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        // Cấu hình strict matching để tránh mapping nhầm
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setAmbiguityIgnored(true);
        return modelMapper;
    }

    /**
     * Initialize default admin account on startup
     */
    @Bean
    public CommandLineRunner initData(AuthService authService) {
        return args -> {
            log.info("Initializing application data...");
            authService.createDefaultAdmin();
            log.info("Application initialization completed");
        };
    }
}
