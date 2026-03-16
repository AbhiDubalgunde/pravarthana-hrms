package com.pravarthana.hrms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Maps to chat_room_members table:
 * id, room_id (NOT NULL), user_id (NOT NULL), joined_at, last_read_at
 * + role VARCHAR(20) [added by 07_chat_production_upgrade.sql]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_room_members", uniqueConstraints = @UniqueConstraint(columnNames = { "room_id", "user_id" }))
public class ChatRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** ADMIN or MEMBER — column added by migration 07 */
    @Column(name = "role", length = 20)
    private String role = "MEMBER";

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null)
            joinedAt = LocalDateTime.now();
        if (role == null)
            role = "MEMBER";
    }
}
