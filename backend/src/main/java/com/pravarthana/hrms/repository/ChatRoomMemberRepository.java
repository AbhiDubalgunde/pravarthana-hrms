package com.pravarthana.hrms.repository;

import com.pravarthana.hrms.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    /** Security check: is user a member of this room? */
    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    /** Get the specific member record (for role check + last_read_at) */
    Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId);

    /** All members of a room */
    List<ChatRoomMember> findByRoomId(Long roomId);

    /** All rooms a user is a member of (IDs only, used in service) */
    List<ChatRoomMember> findByUserId(Long userId);

    /** Remove a single member from a room */
    void deleteByRoomIdAndUserId(Long roomId, Long userId);

    /** Count members in a room */
    long countByRoomId(Long roomId);
}
