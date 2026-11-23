package com.microbms.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
 @Bean BCryptPasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
 @Bean SecurityFilterChain security(HttpSecurity http,LocalJwtSecret secretProvider)throws Exception {
  String secret=secretProvider.get();
  byte[] bytes; try{bytes=Base64.getDecoder().decode(secret);}catch(IllegalArgumentException e){bytes=secret.getBytes(StandardCharsets.UTF_8);}
  var key=Keys.hmacShaKeyFor(bytes);
  OncePerRequestFilter jwtFilter=new OncePerRequestFilter(){
   @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException {
    String auth=request.getHeader("Authorization");
    if(auth!=null&&auth.startsWith("Bearer "))try{
     String subject=Jwts.parser().verifyWith(key).build().parseSignedClaims(auth.substring(7)).getPayload().getSubject();
     if(subject!=null){request.setAttribute("authenticatedUserId",subject);var principal=new UsernamePasswordAuthenticationToken(subject,null,AuthorityUtils.NO_AUTHORITIES);org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(principal);}
    }catch(RuntimeException ignored){org.springframework.security.core.context.SecurityContextHolder.clearContext();}
    chain.doFilter(request,response);
   }
  };
  return http.csrf(c->c.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(a->a.requestMatchers("/api/auth/register","/api/auth/login","/actuator/health").permitAll().anyRequest().authenticated()).addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class).build();
 }
}
