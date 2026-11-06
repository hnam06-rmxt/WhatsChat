package com.example.chat.repository;

import com.example.chat.entity.Conversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query(value = """
        SELECT c.id FROM conversations c
        WHERE c.type = 'DIRECT'
          AND EXISTS (SELECT 1 FROM conversation_members m WHERE m.conversation_id = c.id AND m.user_id = :a)
          AND EXISTS (SELECT 1 FROM conversation_members m WHERE m.conversation_id = c.id AND m.user_id = :b)
        LIMIT 1
        """, nativeQuery = true)
    Optional<Long> findDirectId(@Param("a") Long a, @Param("b") Long b);

    @Query(value = """
        SELECT c.* FROM conversations c
        JOIN conversation_members m ON m.conversation_id = c.id
        WHERE m.user_id = :userId
        ORDER BY COALESCE(c.last_message_id, 0) DESC, c.id DESC
        """, nativeQuery = true)
    List<Conversation> findByMember(@Param("userId") Long userId);
}
