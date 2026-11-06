package com.example.chat.controller;

import com.example.chat.dto.Dtos.*;
import com.example.chat.service.AuthService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/auth/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest r) { return auth.register(r); }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) { return auth.login(r); }

    @GetMapping("/users/me")
    public UserDto me(Authentication a) { return auth.me((Long) a.getPrincipal()); }

    @GetMapping("/users")
    public List<UserDto> search(@RequestParam String q) { return auth.search(q); }
}
