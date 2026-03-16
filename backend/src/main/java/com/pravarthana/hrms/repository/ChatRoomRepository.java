package com.pravarthana.hrms.repository;

import com.pravarthana.hrms.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * Returns only the rooms a specific user is a member of.
     * Joins with chat_room_members table.
     */
    @Query("SELECT r FROM ChatRoom r JOIN ChatRoomMember m ON m.roomId = r.id " +
            "WHERE m.userId = :userId ORDER BY r.createdAt DESC")
    List<ChatRoom> findRoomsByUserId(@Param("userId") Long userId);

    /** Returns all rooms ordered by creation date */
    List<ChatRoom> findAllByOrderByCreatedAtAsc();

    /** Check if a DIRECT room exists between two users */
    @Query("SELECT r FROM ChatRoom r WHERE r.roomType = 'direct' " +
            "AND EXISTS (SELECT m FROM ChatRoomMember m WHERE m.roomId = r.id AND m.userId = :userA) " +
            "AND EXISTS (SELECT m FROM ChatRoomMember m WHERE m.roomId = r.id AND m.userId = :userB)")
    List<ChatRoom> findDirectRoomBetween(@Param("userA") Long userA, @Param("userB") Long userB);
}
