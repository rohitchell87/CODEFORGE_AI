package com.codeforge.ai.controller;

import com.codeforge.ai.dto.ApiResponse;
import com.codeforge.ai.dto.DashboardStatsDto;
import com.codeforge.ai.security.UserPrincipal;
import com.codeforge.ai.service.DashboardService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@AllArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getOverview() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        DashboardStatsDto stats = dashboardService.getDashboardOverview(userPrincipal.getId());
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Dashboard overview retrieved successfully", stats));
    }
}