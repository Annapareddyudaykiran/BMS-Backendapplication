package com.microbms.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key; private final long expirationMinutes;
    public JwtService(LocalJwtSecret secretProvider, @org.springframework.beans.factory.annotation.Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        String secret = secretProvider.get();
        if(expirationMinutes < 1) throw new IllegalStateException("JWT_EXPIRATION_MINUTES must be positive");
        byte[] bytes;
        try { bytes=java.util.Base64.getDecoder().decode(secret); } catch (IllegalArgumentException ex) { bytes=secret.getBytes(java.nio.charset.StandardCharsets.UTF_8); }
        if(bytes.length<32) throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes (prefer a Base64-encoded random key)");
        this.key=Keys.hmacShaKeyFor(bytes); this.expirationMinutes=expirationMinutes;
    }
    public String issue(UserAccount user) { Instant now=Instant.now(); return Jwts.builder().subject(user.getId().toString()).claim("email",user.getEmail()).claim("name",user.getFullName()).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(expirationMinutes*60))).signWith(key).compact(); }
    public long expirationSeconds() { return expirationMinutes * 60; }
}
