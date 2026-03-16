package com.pravarthana.hrms.repository;

import com.pravarthana.hrms.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /** Messages for a room ordered by creation time (no company_id in DB) */
    List<ChatMessage> findByRoomIdAndIsDeletedFalseOrderByCreatedAtAsc(Long roomId);

    Page<ChatMessage> findByRoomIdAndIsDeletedFalseOrderByCreatedAtDesc(Long roomId, Pageable pageable);

    /**
     * Count unread messages in a room after a given timestamp (for unread badge)
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.roomId = :roomId AND m.isDeleted = false AND m.createdAt > :since")
    long countUnreadInRoom(@Param("roomId") Long roomId, @Param("since") LocalDateTime since);

    /** Count all messages per room */
    long countByRoomId(Long roomId);
}
