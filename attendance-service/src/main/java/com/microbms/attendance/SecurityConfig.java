package com.microbms.attendance;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain security(HttpSecurity http, LocalJwtSecret secretProvider) throws Exception {
        String secret = secretProvider.get();
        byte[] bytes;
        try { bytes = Base64.getDecoder().decode(secret); }
        catch (IllegalArgumentException e) { bytes = secret.getBytes(StandardCharsets.UTF_8); }
        var key = Keys.hmacShaKeyFor(bytes);

        OncePerRequestFilter jwtFilter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain) throws ServletException, IOException {
                String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (authorization != null && authorization.startsWith("Bearer ")) {
                    try {
                        Claims claims = Jwts.parser().verifyWith(key).build()
                                .parseSignedClaims(authorization.substring(7)).getPayload();
                        UUID userId = UUID.fromString(claims.getSubject());
                        String email = claims.get("email", String.class);
                        if (email != null && !email.isBlank()) {
                            request.setAttribute("authenticatedUserId", userId);
                            request.setAttribute("authenticatedUserEmail", email);
                            var principal = new UsernamePasswordAuthenticationToken(userId.toString(), null,
                                    AuthorityUtils.createAuthorityList("ROLE_USER"));
                            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(principal);
                        }
                    } catch (RuntimeException ignored) {
                        org.springframework.security.core.context.SecurityContextHolder.clearContext();
                    }
                }
                chain.doFilter(request, response);
            }
        };
        return http.csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a.anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
