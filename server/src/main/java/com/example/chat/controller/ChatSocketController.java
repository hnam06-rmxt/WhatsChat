package com.example.chat.controller;

import com.example.chat.dto.Dtos.SocketSend;
import com.example.chat.service.MessageService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

/** Gateway WebSocket: khong chua logic, chi goi MessageService (giong REST). */
@Controller
public class ChatSocketController {
    private final MessageService messages;

    public ChatSocketController(MessageService messages) { this.messages = messages; }

    @MessageMapping("/chat.send")
    public void send(Principal principal, @Valid @Payload SocketSend p) {
        messages.send(Long.valueOf(principal.getName()), p.conversationId(), p.content());
    }
}
