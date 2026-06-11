package com.codeforge.ai.controller;

import com.codeforge.ai.dto.ApiResponse;
import com.codeforge.ai.entity.Contest;
import com.codeforge.ai.entity.ContestParticipation;
import com.codeforge.ai.service.ContestService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.codeforge.ai.security.UserPrincipal;

import java.util.List;

@RestController
@RequestMapping("/contests")
@AllArgsConstructor
public class ContestController {

    private ContestService contestService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Contest>>> getAllContests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Contest> contests = contestService.getAllContests(pageable);
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Contests retrieved successfully", contests));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Contest>> getContestById(@PathVariable Long id) {
        Contest contest = contestService.getContestById(id);
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Contest retrieved successfully", contest));
    }

    @PostMapping("/{id}/participate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> participateInContest(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        contestService.participateInContest(userPrincipal.getId(), id);
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Participation successful", ""));
    }

    @PostMapping("/{id}/leave")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> leaveContest(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        contestService.leaveContest(userPrincipal.getId(), id);
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Left contest successfully", ""));
    }

    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<ApiResponse<Page<ContestParticipation>>> getLeaderboard(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ContestParticipation> leaderboard = contestService.getContestLeaderboard(id, pageable);
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Leaderboard retrieved successfully", leaderboard));
    }

    @GetMapping("/user/my-contests")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ContestParticipation>>> getUserContests() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        List<ContestParticipation> contests = contestService.getUserContests(userPrincipal.getId());
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "User contests retrieved successfully", contests));
    }
}