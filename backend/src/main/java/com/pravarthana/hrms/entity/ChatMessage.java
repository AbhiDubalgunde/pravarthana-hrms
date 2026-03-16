package com.pravarthana.hrms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Maps to actual chat_messages DB schema:
 * id, room_id (NOT NULL), sender_id (NOT NULL), message (TEXT NOT NULL),
 * is_read (BOOL), is_deleted (BOOL), created_at, updated_at
 *
 * Removed non-existent columns: company_id, sender_name, sent_at, content
 * Added: is_read, is_deleted, updated_at, message_type
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    /** DB column name is 'message' (not 'content') */
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "message_type", length = 20)
    private String messageType = "TEXT";

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (isRead == null)
            isRead = false;
        if (isDeleted == null)
            isDeleted = false;
        if (messageType == null)
            messageType = "TEXT";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
