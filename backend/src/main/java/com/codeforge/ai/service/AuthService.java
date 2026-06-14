package com.codeforge.ai.service;
import com.codeforge.ai.security.UserPrincipal;
import com.codeforge.ai.dto.AuthResponse;
import com.codeforge.ai.dto.LoginRequest;
import com.codeforge.ai.dto.PasswordResetRequest;
import com.codeforge.ai.dto.PasswordResetStartRequest;
import com.codeforge.ai.dto.PasswordResetVerifyRequest;
import com.codeforge.ai.dto.SecurityQuestionResponse;
import com.codeforge.ai.dto.SignupRequest;
import com.codeforge.ai.entity.Role;
import com.codeforge.ai.security.JwtTokenProvider;
import com.codeforge.ai.security.UserPrincipal;
import com.codeforge.ai.entity.User;
import com.codeforge.ai.exception.DuplicateResourceException;
import com.codeforge.ai.exception.InvalidInputException;
import com.codeforge.ai.exception.ResourceNotFoundException;
import com.codeforge.ai.exception.UnauthorizedException;
import com.codeforge.ai.repository.UserRepository;
import com.codeforge.ai.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse signup(SignupRequest signupRequest) {
        if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
            throw new InvalidInputException("Passwords do not match");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = User.builder()
                .email(signupRequest.getEmail())
                .firstName(signupRequest.getFirstName())
                .lastName(signupRequest.getLastName())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .securityQuestion(signupRequest.getSecurityQuestion())
                .securityAnswerHash(passwordEncoder.encode(signupRequest.getSecurityAnswer()))
                .role(Role.USER)
                .lastSolvedDate(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        String token = jwtTokenProvider.generateTokenFromEmail(user.getEmail(), user.getId());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().toString())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public SecurityQuestionResponse getSecurityQuestion(PasswordResetStartRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getSecurityQuestion() == null || user.getSecurityQuestion().isBlank()) {
            throw new InvalidInputException("This account has not configured password recovery");
        }

        return new SecurityQuestionResponse(user.getSecurityQuestion());
    }

    public void verifySecurityAnswer(PasswordResetVerifyRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String storedAnswerHash = user.getSecurityAnswerHash();
        if (storedAnswerHash == null || !passwordEncoder.matches(request.getSecurityAnswer(), storedAnswerHash)) {
            throw new InvalidInputException("Security answer is incorrect");
        }
    }

    public void resetPassword(PasswordResetRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidInputException("Passwords do not match");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String storedAnswerHash = user.getSecurityAnswerHash();
        if (storedAnswerHash == null || !passwordEncoder.matches(request.getSecurityAnswer(), storedAnswerHash)) {
            throw new InvalidInputException("Security answer is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest loginRequest) {
        // Explicitly validate credentials to return clear 4xx errors instead of 500.
        User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Account does not exist."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        // Build authenticated principal and set security context
        UserPrincipal principal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateTokenFromEmail(user.getEmail(), user.getId());

        return AuthResponse.builder()
            .token(token)
            .type("Bearer")
            .userId(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(user.getRole().toString())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
