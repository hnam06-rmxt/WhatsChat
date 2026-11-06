package com.example.chat.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "conversation_members")
@IdClass(ConversationMember.Key.class)
public class ConversationMember {
    public record Key(Long conversationId, Long userId) implements Serializable {
        public Key() { this(null, null); }
    }

    @Id @Column(name = "conversation_id") private Long conversationId;
    @Id @Column(name = "user_id") private Long userId;
    @Column(name = "last_read_message_id") private Long lastReadMessageId;

    public ConversationMember() {}
    public ConversationMember(Long conversationId, Long userId) {
        this.conversationId = conversationId;
        this.userId = userId;
    }

    public Long getConversationId() { return conversationId; }
    public Long getUserId() { return userId; }
    public Long getLastReadMessageId() { return lastReadMessageId; }
    public void setLastReadMessageId(Long v) { this.lastReadMessageId = v; }
}
