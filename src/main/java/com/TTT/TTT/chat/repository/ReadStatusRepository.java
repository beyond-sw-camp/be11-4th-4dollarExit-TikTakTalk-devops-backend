package com.TTT.TTT.chat.repository;

import com.TTT.TTT.User.domain.User;
import com.TTT.TTT.chat.domain.ChatRoom;
import com.TTT.TTT.chat.domain.ReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {
    List<ReadStatus> findByChatRoomAndUser(ChatRoom chatRoom, User user);
    Long countByChatRoomAndUserAndIsReadFalse(ChatRoom chatRoom, User user);
}
