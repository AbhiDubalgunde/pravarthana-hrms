package com.pravarthana.hrms.controller;

import com.pravarthana.hrms.dto.request.CreateRoomRequest;
import com.pravarthana.hrms.dto.response.ChatMessageResponse;
import com.pravarthana.hrms.dto.response.ChatRoomResponse;
import com.pravarthana.hrms.entity.ChatRoomMember;
import com.pravarthana.hrms.security.TenantContext;
import com.pravarthana.hrms.service.ChatService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // ── Rooms ────────────────────────────────────────────────────────────────

    /** GET /api/chat/rooms — only rooms current user is a member of */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> listRooms() {
        return ResponseEntity.ok(chatService.listRooms());
    }

    /** POST /api/chat/rooms — create a room and add initial members */
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.createRoom(req));
    }

    // ── Members ───────────────────────────────────────────────────────────────

    /** GET /api/chat/rooms/{roomId}/members — list members (membership required) */
    @GetMapping("/rooms/{roomId}/members")
    public ResponseEntity<List<ChatRoomMember>> getMembers(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getMembers(roomId));
    }

    /** POST /api/chat/rooms/{roomId}/members — add members (ADMIN only) */
    @PostMapping("/rooms/{roomId}/members")
    public ResponseEntity<Void> addMembers(@PathVariable Long roomId,
            @RequestBody AddMembersRequest req) {
        chatService.addMembers(roomId, req.getUserIds());
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/chat/rooms/{roomId}/members/{userId} — remove member (ADMIN only)
     */
    @DeleteMapping("/rooms/{roomId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long roomId,
            @PathVariable Long userId) {
        chatService.removeMember(roomId, userId);
        return ResponseEntity.noContent().build();
    }

    /** DELETE /api/chat/rooms/{roomId}/leave — leave a room */
    @DeleteMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(@PathVariable Long roomId) {
        chatService.leaveRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    // ── Messages ──────────────────────────────────────────────────────────────

    /**
     * GET /api/chat/rooms/{roomId}/messages — paginated messages (membership
     * required)
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getMessages(roomId));
    }

    /**
     * POST /api/chat/rooms/{roomId}/messages — send a message (membership required)
     */
    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(@PathVariable Long roomId,
            @RequestBody SendMessageBody body) {
        ChatMessageResponse msg = chatService.sendMessage(roomId, body.getContent());
        // Broadcast via WebSocket
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, msg);
        return ResponseEntity.status(HttpStatus.CREATED).body(msg);
    }

    // ── Unread Count ──────────────────────────────────────────────────────────

    /** GET /api/chat/rooms/{roomId}/unread — unread message count */
    @GetMapping("/rooms/{roomId}/unread")
    public ResponseEntity<Map<String, Long>> getUnread(@PathVariable Long roomId) {
        return ResponseEntity.ok(Map.of("unreadCount", chatService.getUnreadCount(roomId)));
    }

    // ── Direct Messages ───────────────────────────────────────────────────────

    /** POST /api/chat/direct/{targetUserId} — auto-create or fetch DM room */
    @PostMapping("/direct/{targetUserId}")
    public ResponseEntity<ChatRoomResponse> getOrCreateDirect(@PathVariable Long targetUserId) {
        return ResponseEntity.ok(chatService.getOrCreateDirectRoom(targetUserId));
    }

    // ── STOMP WebSocket handler ───────────────────────────────────────────────

    @MessageMapping("/chat.send")
    public void handleStompMessage(@Payload StompMessage req) {
        ChatMessageResponse saved = chatService.saveMessage(
                req.getRoomId(), req.getSenderId(), req.getContent());
        messagingTemplate.convertAndSend("/topic/chat/" + saved.getRoomId(), saved);
    }

    // ── Inner request bodies ──────────────────────────────────────────────────

    @Data
    public static class AddMembersRequest {
        private List<Long> userIds;
    }

    @Data
    public static class SendMessageBody {
        private String content;
    }

    @Data
    public static class StompMessage {
        private Long roomId;
        private Long senderId;
        private String content;
    }
}
