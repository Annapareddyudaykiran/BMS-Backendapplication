package com.microbms.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users; private final BCryptPasswordEncoder encoder; private final JwtService jwt;
    public AuthController(UserRepository users, BCryptPasswordEncoder encoder, JwtService jwt){this.users=users;this.encoder=encoder;this.jwt=jwt;}
    public record RegisterRequest(@NotBlank @Size(max=120) String fullName, @NotBlank @Email @Size(max=254) String email, @NotBlank @Size(min=8,max=72) String password) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record UserView(UUID id,String email,String fullName) {}
    public record TokenResponse(String token,String tokenType,long expiresInSeconds,UserView user) {}
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@Valid @RequestBody RegisterRequest request){String email=request.email().trim().toLowerCase(); if(users.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT,"An account with this email already exists"); UserAccount user=users.save(new UserAccount(email,request.fullName().trim(),encoder.encode(request.password()))); return response(user);}
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request){UserAccount user=users.findByEmail(request.email().trim().toLowerCase()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password")); if(!encoder.matches(request.password(),user.getPasswordHash())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password"); return response(user);}
    @GetMapping("/me") public UserView me(@org.springframework.web.bind.annotation.RequestAttribute("authenticatedUserId") String userId){UserAccount user=users.findById(UUID.fromString(userId)).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User not found")); return view(user);}
    private TokenResponse response(UserAccount u){return new TokenResponse(jwt.issue(u),"Bearer",jwt.expirationSeconds(),view(u));} private UserView view(UserAccount u){return new UserView(u.getId(),u.getEmail(),u.getFullName());}
}
