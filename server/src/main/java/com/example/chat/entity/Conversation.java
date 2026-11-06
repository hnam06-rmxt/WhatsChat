package com.example.chat.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "conversations")
public class Conversation {
    public enum Type { DIRECT, GROUP }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Type type;
    private String name;
    @Column(name = "last_message_id") private Long lastMessageId;
    @Column(name = "created_at", insertable = false, updatable = false) private Instant createdAt;

    public Long getId() { return id; }
    public Type getType() { return type; }
    public void setType(Type v) { this.type = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public Long getLastMessageId() { return lastMessageId; }
    public void setLastMessageId(Long v) { this.lastMessageId = v; }
}
