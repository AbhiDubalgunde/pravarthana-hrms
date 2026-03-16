package com.pravarthana.hrms.service;

import com.pravarthana.hrms.dto.request.CreateRoomRequest;
import com.pravarthana.hrms.dto.response.ChatMessageResponse;
import com.pravarthana.hrms.dto.response.ChatRoomResponse;
import com.pravarthana.hrms.entity.ChatMessage;
import com.pravarthana.hrms.entity.ChatRoom;
import com.pravarthana.hrms.entity.ChatRoomMember;
import com.pravarthana.hrms.repository.ChatMessageRepository;
import com.pravarthana.hrms.repository.ChatRoomMemberRepository;
import com.pravarthana.hrms.repository.ChatRoomRepository;
import com.pravarthana.hrms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * Epoch fallback for users with null last_read_at — PostgreSQL-safe (unlike
     * LocalDateTime.MIN)
     */
    private static final LocalDateTime EPOCH = LocalDateTime.of(2020, 1, 1, 0, 0, 0);

    // ── List rooms (only rooms current user is a member of) ──────────────────
    public List<ChatRoomResponse> listRooms() {
        Long userId = TenantContext.getUserId();
        List<ChatRoom> rooms = chatRoomRepository.findRoomsByUserId(userId);

        // Fetch member memberships to compute unread counts
        List<ChatRoomMember> myMemberships = chatRoomMemberRepository.findByUserId(userId);
        Map<Long, LocalDateTime> lastReadMap = myMemberships.stream()
                .collect(Collectors.toMap(ChatRoomMember::getRoomId,
                        m -> m.getLastReadAt() != null ? m.getLastReadAt() : EPOCH));

        return rooms.stream().map(r -> {
            ChatRoomResponse dto = ChatRoomResponse.from(r);
            // Unread count
            LocalDateTime lastRead = lastReadMap.getOrDefault(r.getId(), EPOCH);
            long unread = chatMessageRepository.countUnreadInRoom(r.getId(), lastRead);
            dto.setUnreadCount(unread);
            dto.setMemberCount((int) chatRoomMemberRepository.countByRoomId(r.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    // ── Create room with initial members (atomic) ────────────────────────────
    @Transactional
    public ChatRoomResponse createRoom(CreateRoomRequest req) {
        Long userId = TenantContext.getUserId();
        if (req.getName() == null || req.getName().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room name is required");

        ChatRoom room = new ChatRoom();
        room.setName(req.getName());
        room.setRoomType(req.getType() != null ? req.getType().toLowerCase() : "group");
        room.setCreatedBy(userId);
        room = chatRoomRepository.save(room);
        log.info("[Chat] Created room id={} name='{}' by userId={}", room.getId(), room.getName(), userId);

        // Add creator as ADMIN
        addMember(room.getId(), userId, "ADMIN");

        // Add other initial members as MEMBER
        if (req.getMemberIds() != null) {
            for (Long memberId : req.getMemberIds()) {
                if (!memberId.equals(userId)) {
                    addMember(room.getId(), memberId, "MEMBER");
                }
            }
        }

        ChatRoomResponse dto = ChatRoomResponse.from(room);
        dto.setMemberCount((int) chatRoomMemberRepository.countByRoomId(room.getId()));
        dto.setUnreadCount(0L);
        return dto;
    }

    // ── Add members to room (only room ADMIN can do this) ────────────────────
    @Transactional
    public void addMembers(Long roomId, List<Long> userIds) {
        Long requesterId = TenantContext.getUserId();
        assertMemberWithRole(roomId, requesterId, "ADMIN");

        for (Long uid : userIds) {
            if (!chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, uid)) {
                addMember(roomId, uid, "MEMBER");
                log.info("[Chat] Added userId={} to roomId={} by userId={}", uid, roomId, requesterId);
            }
        }
    }

    // ── Remove member from room (ADMIN only, cannot remove self if only admin) ─
    @Transactional
    public void removeMember(Long roomId, Long targetUserId) {
        Long requesterId = TenantContext.getUserId();
        assertMemberWithRole(roomId, requesterId, "ADMIN");
        chatRoomMemberRepository.deleteByRoomIdAndUserId(roomId, targetUserId);
        log.info("[Chat] Removed userId={} from roomId={} by userId={}", targetUserId, roomId, requesterId);
    }

    // ── Leave room ────────────────────────────────────────────────────────────
    @Transactional
    public void leaveRoom(Long roomId) {
        Long userId = TenantContext.getUserId();
        assertMember(roomId, userId);
        chatRoomMemberRepository.deleteByRoomIdAndUserId(roomId, userId);
        log.info("[Chat] userId={} left roomId={}", userId, roomId);
    }

    // ── Get messages (membership validated) ─────────────────────────────────
    public List<ChatMessageResponse> getMessages(Long roomId) {
        Long userId = TenantContext.getUserId();
        assertMember(roomId, userId);

        List<ChatMessageResponse> messages = chatMessageRepository
                .findByRoomIdAndIsDeletedFalseOrderByCreatedAtAsc(roomId)
                .stream().map(ChatMessageResponse::from).collect(Collectors.toList());

        // Update last_read_at for this user
        chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId).ifPresent(m -> {
            m.setLastReadAt(LocalDateTime.now());
            chatRoomMemberRepository.save(m);
        });

        return messages;
    }

    // ── Send message (membership validated) ──────────────────────────────────
    @Transactional
    public ChatMessageResponse sendMessage(Long roomId, String content) {
        Long userId = TenantContext.getUserId();
        assertMember(roomId, userId);

        if (content == null || content.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content cannot be empty");

        ChatMessage msg = new ChatMessage();
        msg.setRoomId(roomId);
        msg.setSenderId(userId);
        msg.setMessage(content);
        msg.setMessageType("TEXT");
        msg = chatMessageRepository.save(msg);

        // Update sender's last_read_at
        chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId).ifPresent(m -> {
            m.setLastReadAt(LocalDateTime.now());
            chatRoomMemberRepository.save(m);
        });

        log.info("[Chat] Message sent: roomId={} senderId={} msgId={}", roomId, userId, msg.getId());
        return ChatMessageResponse.from(msg);
    }

    // ── Get or create DIRECT room between two users ───────────────────────────
    @Transactional
    public ChatRoomResponse getOrCreateDirectRoom(Long targetUserId) {
        Long userId = TenantContext.getUserId();
        if (userId.equals(targetUserId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create DM with yourself");

        List<ChatRoom> existing = chatRoomRepository.findDirectRoomBetween(userId, targetUserId);
        if (!existing.isEmpty()) {
            ChatRoomResponse dto = ChatRoomResponse.from(existing.get(0));
            dto.setMemberCount(2);
            return dto;
        }

        // Create new DIRECT room
        CreateRoomRequest req = new CreateRoomRequest();
        req.setName("DM");
        req.setMemberIds(List.of(targetUserId));
        // Override type to direct
        ChatRoom room = new ChatRoom();
        room.setName("direct");
        room.setRoomType("direct");
        room.setCreatedBy(userId);
        room = chatRoomRepository.save(room);
        addMember(room.getId(), userId, "ADMIN");
        addMember(room.getId(), targetUserId, "MEMBER");

        log.info("[Chat] Created DM room id={} between userId={} and userId={}", room.getId(), userId, targetUserId);
        ChatRoomResponse dto = ChatRoomResponse.from(room);
        dto.setMemberCount(2);
        dto.setUnreadCount(0L);
        return dto;
    }

    // ── Get unread count for a room ───────────────────────────────────────────
    public long getUnreadCount(Long roomId) {
        Long userId = TenantContext.getUserId();
        assertMember(roomId, userId);
        LocalDateTime lastRead = chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .map(m -> m.getLastReadAt() != null ? m.getLastReadAt() : LocalDateTime.MIN)
                .orElse(LocalDateTime.MIN);
        return chatMessageRepository.countUnreadInRoom(roomId, lastRead);
    }

    // ── Get room members ──────────────────────────────────────────────────────
    public List<ChatRoomMember> getMembers(Long roomId) {
        Long userId = TenantContext.getUserId();
        assertMember(roomId, userId);
        return chatRoomMemberRepository.findByRoomId(roomId);
    }

    // ── STOMP-compatible save (called from WebSocket handler) ─────────────────
    @Transactional
    public ChatMessageResponse saveMessage(Long roomId, Long senderId, String content) {
        // Validate membership
        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, senderId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this room");
        }
        ChatMessage msg = new ChatMessage();
        msg.setRoomId(roomId);
        msg.setSenderId(senderId);
        msg.setMessage(content);
        msg.setMessageType("TEXT");
        return ChatMessageResponse.from(chatMessageRepository.save(msg));
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private void addMember(Long roomId, Long userId, String role) {
        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            ChatRoomMember m = new ChatRoomMember();
            m.setRoomId(roomId);
            m.setUserId(userId);
            m.setRole(role);
            chatRoomMemberRepository.save(m);
        }
    }

    private void assertMember(Long roomId, Long userId) {
        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this room");
        }
    }

    private void assertMemberWithRole(Long roomId, Long userId, String requiredRole) {
        ChatRoomMember member = chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this room"));
        if (!requiredRole.equalsIgnoreCase(member.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only room " + requiredRole + " can perform this action");
        }
    }
}
