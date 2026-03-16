package com.pravarthana.hrms.dto.response;

import com.pravarthana.hrms.entity.ChatRoom;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatRoomResponse {
    private Long id;
    private String name;
    private String type; // group or direct
    private Long createdBy;
    private LocalDateTime createdAt;
    private Boolean isActive;
    private Long unreadCount; // populated by service
    private Integer memberCount; // populated by service

    public static ChatRoomResponse from(ChatRoom r) {
        ChatRoomResponse dto = new ChatRoomResponse();
        dto.setId(r.getId());
        dto.setName(r.getName());
        dto.setType(r.getRoomType());
        dto.setCreatedBy(r.getCreatedBy());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }
}
