package com.pravarthana.hrms.dto.response;

import com.pravarthana.hrms.entity.ChatMessage;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageResponse {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String message; // DB col is 'message' not 'content'
    private String messageType;
    private Boolean isDeleted;
    private LocalDateTime createdAt;

    public static ChatMessageResponse from(ChatMessage m) {
        ChatMessageResponse dto = new ChatMessageResponse();
        dto.setId(m.getId());
        dto.setRoomId(m.getRoomId());
        dto.setSenderId(m.getSenderId());
        dto.setMessage(m.getMessage());
        dto.setMessageType(m.getMessageType());
        dto.setIsDeleted(m.getIsDeleted());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
}
