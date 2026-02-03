package com.documindai.security;

import com.documindai.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Entry point xử lý authentication errors
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        ApiResponse<Object> apiResponse = new ApiResponse<>(
            false,
            "Unauthorized: " + authException.getMessage(),
            null,
            LocalDateTime.now()
        );
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // Để serialize LocalDateTime
        
        response.getWriter().write(mapper.writeValueAsString(apiResponse));
    }
}
