package com.example.chat.service;

import com.example.chat.dto.Dtos.*;
import com.example.chat.entity.User;
import com.example.chat.repository.UserRepository;
import com.example.chat.security.JwtService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    public AuthResponse register(RegisterRequest r) {
        if (users.existsByUsername(r.username()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username da ton tai");
        User u = new User();
        u.setUsername(r.username().trim().toLowerCase());
        u.setDisplayName(r.displayName().trim());
        u.setPasswordHash(encoder.encode(r.password()));
        users.save(u);
        return toAuth(u);
    }

    public AuthResponse login(LoginRequest r) {
        User u = users.findByUsername(r.username().trim().toLowerCase())
                .filter(x -> encoder.matches(r.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai tai khoan hoac mat khau"));
        return toAuth(u);
    }

    public UserDto me(Long id) {
        return users.findById(id).map(AuthService::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    public List<UserDto> search(String q) {
        return users.findTop20ByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(q, q)
                .stream().map(AuthService::toDto).toList();
    }

    private AuthResponse toAuth(User u) { return new AuthResponse(jwt.generate(u.getId(), u.getUsername()), toDto(u)); }
    static UserDto toDto(User u) { return new UserDto(u.getId(), u.getUsername(), u.getDisplayName()); }
}
