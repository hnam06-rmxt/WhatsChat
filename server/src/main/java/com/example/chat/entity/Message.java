package com.example.chat.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "messages")
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "conversation_id", nullable = false) private Long conversationId;
    @Column(name = "sender_id", nullable = false) private Long senderId;
    @Column(nullable = false, columnDefinition = "text") private String content;
    @Column(name = "created_at", insertable = false, updatable = false) private Instant createdAt;

    public Long getId() { return id; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long v) { this.conversationId = v; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long v) { this.senderId = v; }
    public String getContent() { return content; }
    public void setContent(String v) { this.content = v; }
    public Instant getCreatedAt() { return createdAt; }
}
