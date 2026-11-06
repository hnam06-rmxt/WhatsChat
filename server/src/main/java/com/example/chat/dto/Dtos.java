package com.example.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class Dtos {
    private Dtos() {}

    public record RegisterRequest(@NotBlank @Size(min = 3, max = 50) String username,
                                  @NotBlank @Size(max = 100) String displayName,
                                  @NotBlank @Size(min = 6, max = 72) String password) {}
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record UserDto(Long id, String username, String displayName) {}
    public record AuthResponse(String token, UserDto user) {}

    public record DirectRequest(@NotNull Long userId) {}
    public record GroupRequest(@NotBlank String name, @NotEmpty List<Long> memberIds) {}
    public record SendRequest(@NotBlank @Size(max = 5000) String content) {}
    public record SocketSend(@NotNull Long conversationId, @NotBlank @Size(max = 5000) String content) {}
    public record ReadRequest(@NotNull Long messageId) {}

    public record MessageDto(Long id, Long conversationId, Long senderId, String senderName,
                             String content, Instant createdAt) {}
    public record ConversationDto(Long id, String type, String name, MessageDto lastMessage, long unread) {}
}
