package com.example.chat.service;

import com.example.chat.dto.Dtos.*;
import com.example.chat.entity.Conversation;
import com.example.chat.entity.ConversationMember;
import com.example.chat.repository.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConversationService {
    private final ConversationRepository conversations;
    private final ConversationMemberRepository members;
    private final MessageRepository messages;
    private final UserRepository users;
    private final MessageService messageService;

    public ConversationService(ConversationRepository conversations, ConversationMemberRepository members,
                               MessageRepository messages, UserRepository users, MessageService messageService) {
        this.conversations = conversations; this.members = members;
        this.messages = messages; this.users = users; this.messageService = messageService;
    }

    @Transactional
    public ConversationDto openDirect(Long me, Long other) {
        if (me.equals(other)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Khong the chat voi chinh minh");
        users.findById(other).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong thay user"));
        Long id = conversations.findDirectId(me, other).orElseGet(() -> {
            Conversation c = new Conversation();
            c.setType(Conversation.Type.DIRECT);
            c = conversations.save(c);
            members.save(new ConversationMember(c.getId(), me));
            members.save(new ConversationMember(c.getId(), other));
            return c.getId();
        });
        return toDto(conversations.findById(id).orElseThrow(), me);
    }

    @Transactional
    public ConversationDto createGroup(Long me, GroupRequest r) {
        Set<Long> ids = new LinkedHashSet<>(r.memberIds());
        ids.add(me);
        Conversation c = new Conversation();
        c.setType(Conversation.Type.GROUP);
        c.setName(r.name().trim());
        c = conversations.save(c);
        for (Long uid : ids) {
            users.findById(uid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong thay user " + uid));
            members.save(new ConversationMember(c.getId(), uid));
        }
        return toDto(c, me);
    }

    @Transactional(readOnly = true)
    public List<ConversationDto> list(Long me) {
        List<ConversationDto> out = new ArrayList<>();
        for (Conversation c : conversations.findByMember(me)) out.add(toDto(c, me));
        return out;
    }

    private ConversationDto toDto(Conversation c, Long me) {
        String name = c.getName();
        if (c.getType() == Conversation.Type.DIRECT) {
            name = members.findByConversationId(c.getId()).stream()
                    .map(ConversationMember::getUserId).filter(id -> !id.equals(me)).findFirst()
                    .flatMap(users::findById).map(u -> u.getDisplayName()).orElse("?");
        }
        MessageDto last = c.getLastMessageId() == null ? null
                : messages.findById(c.getLastMessageId()).map(messageService::toDto).orElse(null);
        long lastRead = members.findByConversationIdAndUserId(c.getId(), me)
                .map(m -> m.getLastReadMessageId() == null ? 0L : m.getLastReadMessageId()).orElse(0L);
        long unread = messages.countUnread(c.getId(), me, lastRead);
        return new ConversationDto(c.getId(), c.getType().name(), name, last, unread);
    }
}
