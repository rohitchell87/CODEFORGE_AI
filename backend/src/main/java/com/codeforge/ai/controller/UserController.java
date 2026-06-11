package com.codeforge.ai.controller;

import com.codeforge.ai.dto.ApiResponse;
import com.codeforge.ai.dto.UserProfileDto;
import com.codeforge.ai.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.codeforge.ai.security.UserPrincipal;
import java.util.List;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private UserService userService;

    @GetMapping("/profile/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileDto>> getUserProfile(@PathVariable Long id) {
        UserProfileDto userProfile = userService.getUserProfile(id);
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "User profile retrieved", userProfile));
    }

    @GetMapping("/profile/email/{email}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileDto>> getUserByEmail(@PathVariable String email) {
        UserProfileDto userProfile = userService.getUserProfileByEmail(email);
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "User profile retrieved", userProfile));
    }

    @GetMapping("/stats/{id}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getUserStats(@PathVariable Long id) {
        UserProfileDto stats = userService.getUserProfile(id);
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "User stats retrieved", stats));
    }

    @GetMapping("/me/solved-problems")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Long>>> getSolvedProblems() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        List<Long> solvedProblems = userService.getSolvedProblems(userPrincipal.getId());
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Solved problems retrieved", solvedProblems));
    }
}