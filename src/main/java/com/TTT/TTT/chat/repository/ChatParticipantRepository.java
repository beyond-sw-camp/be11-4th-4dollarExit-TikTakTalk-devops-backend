package com.TTT.TTT.chat.repository;

import com.TTT.TTT.User.domain.User;
import com.TTT.TTT.chat.domain.ChatParticipant;
import com.TTT.TTT.chat.domain.ChatRoom;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    List<ChatParticipant> findByChatRoomAndExitYN(ChatRoom chatRoom, Enum exitYN);
    Optional<ChatParticipant> findByChatRoomAndUser(ChatRoom chatRoom, User user);
    List<ChatParticipant> findAllByUserAndExitYN(User user, Enum exitYN);
    Optional<ChatParticipant> findByUserAndExitYNAndChatRoom(User user, Enum exitYN, ChatRoom chatRoom);

    @Query("SELECT cp1.chatRoom FROM ChatParticipant cp1 JOIN ChatParticipant cp2 ON cp1.chatRoom.id = cp2.chatRoom.id WHERE cp1.user.id = :myId AND cp2.user.id = :otherUserId AND cp1.chatRoom.isGroupChat = 'N'")
    Optional<ChatRoom> findExistingPrivateRoom(@Param("myId") Long myId, @Param("otherUserId") Long otherUserId);
    @Query("SELECT cp FROM ChatParticipant cp JOIN FETCH cp.user WHERE cp.chatRoom.id = :chatRoomId AND cp.isConnected = false")
    List<ChatParticipant> findByChatRoomIdAndIsConnectedFalse(@Param("chatRoomId") Long chatRoomId);


}
