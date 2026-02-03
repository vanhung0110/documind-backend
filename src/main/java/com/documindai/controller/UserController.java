package com.documindai.controller;

import com.documindai.dto.response.ApiResponse;
import com.documindai.dto.response.UserResponse;
import com.documindai.model.User;
import com.documindai.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho user operations
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class UserController {

    private final UserService userService;

    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("User {} fetching profile", userDetails.getUsername());

        User user = userService.getUserByUsername(userDetails.getUsername());

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole().toString());
        response.setActive(user.getActive());
        response.setCreatedAt(user.getCreatedAt());

        return ResponseEntity.ok(ApiResponse.success(response, "Profile retrieved successfully"));
    }
}
