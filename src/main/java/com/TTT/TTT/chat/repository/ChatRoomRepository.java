package com.TTT.TTT.chat.repository;

import com.TTT.TTT.Common.domain.ExitYN;
import com.TTT.TTT.chat.domain.ChatRoom;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByIsGroupChat(String isGroupChat);
    Page<ChatRoom> findByIsGroupChat(Specification<ChatRoom> specification, String isGroupChat, Pageable pageable);
    Page<ChatRoom> findAll(Specification<ChatRoom> specification, Pageable pageable);
    Optional<ChatRoom> findByName(String roomName);
    Optional<ChatRoom> findByIdAndExitYN(Long id, Enum exitYN);

    @Query("SELECT cr FROM ChatRoom cr " +
            "LEFT JOIN FETCH cr.chatParticipants cp " +
            "LEFT JOIN FETCH cr.chatMessages cm " +
            "LEFT JOIN FETCH cm.user " +
            "WHERE cr.id = :roomId AND cr.exitYN = :exitYN")
    Optional<ChatRoom> findByIdWithParticipantsAndMessages(
            @Param("roomId") Long roomId,
            @Param("exitYN") ExitYN exitYN
    );

    @Query(value = "SELECT COUNT(*) FROM chat_room WHERE exityn = 'N'", nativeQuery = true)
    Long countTotalRooms();
}
