package com.TTT.TTT.chat.repository;

import com.TTT.TTT.chat.domain.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByIsGroupChat(String isGroupChat);
    Page<ChatRoom> findByIsGroupChat(Specification<ChatRoom> specification, String isGroupChat, Pageable pageable);
    Page<ChatRoom> findAll(Specification<ChatRoom> specification, Pageable pageable);
    Optional<ChatRoom> findByName(String roomName);
}
