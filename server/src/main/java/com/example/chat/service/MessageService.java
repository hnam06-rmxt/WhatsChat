package com.example.chat.service;

import com.example.chat.dto.Dtos.MessageDto;
import com.example.chat.entity.Conversation;
import com.example.chat.entity.ConversationMember;
import com.example.chat.entity.Message;
import com.example.chat.repository.*;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Mot duong duy nhat de gui tin: REST va WebSocket deu goi send(). */
@Service
public class MessageService {
    private final MessageRepository messages;
    private final ConversationRepository conversations;
    private final ConversationMemberRepository members;
    private final UserRepository users;
    private final SimpMessagingTemplate broker;

    public MessageService(MessageRepository messages, ConversationRepository conversations,
                          ConversationMemberRepository members, UserRepository users,
                          SimpMessagingTemplate broker) {
        this.messages = messages; this.conversations = conversations;
        this.members = members; this.users = users; this.broker = broker;
    }

    @Transactional
    public MessageDto send(Long userId, Long conversationId, String content) {
        assertMember(conversationId, userId);
        Message m = new Message();
        m.setConversationId(conversationId);
        m.setSenderId(userId);
        m.setContent(content.trim());
        m = messages.saveAndFlush(m);
        messages.flush();
        Message saved = messages.findById(m.getId()).orElseThrow(); // lay created_at do DB sinh
        Conversation c = conversations.findById(conversationId).orElseThrow();
        c.setLastMessageId(saved.getId());
        MessageDto dto = toDto(saved);
        markRead(userId, conversationId, saved.getId()); // nguoi gui coi nhu da doc
        broker.convertAndSend("/topic/conversations." + conversationId, dto);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<MessageDto> history(Long userId, Long conversationId, Long beforeId, int limit) {
        assertMember(conversationId, userId);
        int size = Math.min(Math.max(limit, 1), 100);
        return messages.page(conversationId, beforeId, PageRequest.of(0, size)).stream().map(this::toDto).toList();
    }

    @Transactional
    public void markRead(Long userId, Long conversationId, Long messageId) {
        ConversationMember cm = members.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        if (cm.getLastReadMessageId() == null || messageId > cm.getLastReadMessageId())
            cm.setLastReadMessageId(messageId);
    }

    public void assertMember(Long conversationId, Long userId) {
        if (!members.existsByConversationIdAndUserId(conversationId, userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Khong phai thanh vien cuoc tro chuyen");
    }

    MessageDto toDto(Message m) {
        String name = users.findById(m.getSenderId()).map(u -> u.getDisplayName()).orElse("?");
        return new MessageDto(m.getId(), m.getConversationId(), m.getSenderId(), name, m.getContent(), m.getCreatedAt());
    }
}
