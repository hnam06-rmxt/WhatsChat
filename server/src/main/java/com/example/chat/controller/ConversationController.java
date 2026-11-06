package com.example.chat.controller;

import com.example.chat.dto.Dtos.*;
import com.example.chat.service.ConversationService;
import com.example.chat.service.MessageService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
    private final ConversationService conversations;
    private final MessageService messages;

    public ConversationController(ConversationService conversations, MessageService messages) {
        this.conversations = conversations; this.messages = messages;
    }

    private static Long uid(Authentication a) { return (Long) a.getPrincipal(); }

    @GetMapping
    public List<ConversationDto> list(Authentication a) { return conversations.list(uid(a)); }

    @PostMapping("/direct")
    public ConversationDto direct(Authentication a, @Valid @RequestBody DirectRequest r) {
        return conversations.openDirect(uid(a), r.userId());
    }

    @PostMapping("/group")
    public ConversationDto group(Authentication a, @Valid @RequestBody GroupRequest r) {
        return conversations.createGroup(uid(a), r);
    }

    /** Lich su, moi nhat truoc. Trang tiep theo: truyen beforeId = id nho nhat da nhan. */
    @GetMapping("/{id}/messages")
    public List<MessageDto> history(Authentication a, @PathVariable Long id,
                                    @RequestParam(required = false) Long beforeId,
                                    @RequestParam(defaultValue = "30") int limit) {
        return messages.history(uid(a), id, beforeId, limit);
    }

    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto send(Authentication a, @PathVariable Long id, @Valid @RequestBody SendRequest r) {
        return messages.send(uid(a), id, r.content());
    }

    @PostMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void read(Authentication a, @PathVariable Long id, @Valid @RequestBody ReadRequest r) {
        messages.markRead(uid(a), id, r.messageId());
    }
}
