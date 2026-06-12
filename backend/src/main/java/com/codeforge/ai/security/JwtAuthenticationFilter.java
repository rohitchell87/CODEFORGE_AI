package com.codeforge.ai.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@AllArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private JwtTokenProvider tokenProvider;
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String requestUri = request.getRequestURI();
            String rawAuthorizationHeader = request.getHeader("Authorization");
            String jwt = getJwtFromRequest(request);
            
            System.out.println("\n=== JWT FILTER DEBUG ===");
            System.out.println("REQUEST URI = " + requestUri);
            System.out.println("RAW AUTHORIZATION HEADER = " + rawAuthorizationHeader);
            System.out.println("EXTRACTED JWT PRESENT = " + (jwt != null));
            if (jwt != null) {
                System.out.println("EXTRACTED JWT LENGTH = " + jwt.length());
            }
            
            log.debug("JwtAuthenticationFilter: incoming request URI={} Authorization header='{}' extractedJwtPresent={}",
                    requestUri, rawAuthorizationHeader, jwt != null);
            
            if (StringUtils.hasText(jwt)) {
                String normalized = jwt.replaceAll("\\s+", "");
                String fingerprint = computeFingerprint(normalized);
                System.out.println("JWT FINGERPRINT = " + fingerprint);
                System.out.println("JWT LENGTH = " + normalized.length());
                
                log.debug("JWT fingerprint={} length={} for request URI={}", fingerprint, normalized.length(), requestUri);
                if (tokenProvider.validateToken(normalized)) {
                    jwt = normalized;
                    System.out.println("JWT VALIDATION PASSED");
                    log.debug("JWT validated fingerprint={} for request URI={}", fingerprint, requestUri);
                } else {
                    System.out.println("JWT VALIDATION FAILED");
                    log.debug("JWT validation failed fingerprint={} for request URI={}", fingerprint, requestUri);
                }
            }

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String email = tokenProvider.getEmailFromToken(jwt);
                UserPrincipal userPrincipal = customUserDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userPrincipal, null, userPrincipal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("AUTHENTICATION SET");
                System.out.println("AUTH USER = " + userPrincipal.getEmail());
                System.out.println("AUTHORITIES = " + userPrincipal.getAuthorities());
                System.out.println("DB ROLE = " + userPrincipal.getRole());
                
                log.info("AUTH USER = {}", userPrincipal.getEmail());
                log.info("AUTHORITIES = {}", userPrincipal.getAuthorities());
                log.info("DB ROLE = {}", userPrincipal.getRole());
                log.info("REQUEST URI = {}", requestUri);
            } else {
                System.out.println("NO VALID JWT - AUTHENTICATION NOT SET");
                System.out.println("JWT hasText = " + StringUtils.hasText(jwt));
                System.out.println("VALIDATION RESULT = " + (StringUtils.hasText(jwt) ? tokenProvider.validateToken(jwt) : false));
                log.debug("No valid JWT for request URI={}", requestUri);
            }
        } catch (Exception ex) {
            System.out.println("EXCEPTION IN JWT FILTER: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            ex.printStackTrace();
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (!StringUtils.hasText(bearerToken)) {
            return null;
        }
        String token;
        if (bearerToken.startsWith("Bearer ") || bearerToken.startsWith("bearer ")) {
            token = bearerToken.substring(7);
        } else {
            token = bearerToken;
        }
        if (!StringUtils.hasText(token)) return null;
        String trimmed = token.trim();
        // Remove any accidental whitespace/newlines inside the token
        String normalized = trimmed.replaceAll("\\s+", "");
        // Strip surrounding quotes if present (some clients send quoted tokens)
        normalized = normalized.replaceAll("^\"|\"$", "");
        normalized = normalized.replaceAll("^'|'$", "");
        return StringUtils.hasText(normalized) ? normalized : null;
    }

    private String computeFingerprint(String token) {
        if (token == null) return "null";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6 && i < digest.length; i++) { // truncate for brevity
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "na";
        }
    }
}
