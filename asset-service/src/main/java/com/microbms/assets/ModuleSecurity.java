package com.microbms.assets;

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

@Configuration
public class ModuleSecurity {
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, LocalJwtSecret secrets) throws Exception {
        String secret=secrets.get(); byte[] bytes;
        try { bytes=Base64.getDecoder().decode(secret); } catch (IllegalArgumentException ex) { bytes=secret.getBytes(StandardCharsets.UTF_8); }
        var key=Keys.hmacShaKeyFor(bytes);
        OncePerRequestFilter filter=new OncePerRequestFilter() {
            @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
                String header=request.getHeader(HttpHeaders.AUTHORIZATION);
                if(header!=null && header.startsWith("Bearer ")) try {
                    Claims claims=Jwts.parser().verifyWith(key).build().parseSignedClaims(header.substring(7)).getPayload();
                    String subject=claims.getSubject(); String email=claims.get("email",String.class); if(subject!=null && email!=null) { request.setAttribute("authenticatedUserId",subject); request.setAttribute("authenticatedUserEmail",email); org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(subject,null,AuthorityUtils.createAuthorityList("ROLE_USER"))); }
                } catch(RuntimeException ignored) { org.springframework.security.core.context.SecurityContextHolder.clearContext(); }
                chain.doFilter(request,response);
            }
        };
        return http.csrf(c->c.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(a->a.anyRequest().authenticated()).addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class).build();
    }
}

