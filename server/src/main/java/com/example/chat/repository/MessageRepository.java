package com.example.chat.repository;

import com.example.chat.entity.Message;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /** Phan trang cursor: lay `limit` tin cu hon beforeId (khong dung OFFSET). */
    @Query("SELECT m FROM Message m WHERE m.conversationId = :cid AND (:beforeId IS NULL OR m.id < :beforeId) ORDER BY m.id DESC")
    List<Message> page(@Param("cid") Long cid, @Param("beforeId") Long beforeId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversationId = :cid AND m.senderId <> :uid AND m.id > :lastRead")
    long countUnread(@Param("cid") Long cid, @Param("uid") Long uid, @Param("lastRead") Long lastRead);
}
