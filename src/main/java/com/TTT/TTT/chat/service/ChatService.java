package com.TTT.TTT.chat.service;

import com.TTT.TTT.User.UserRepository.UserRepository;
import com.TTT.TTT.chat.repository.ChatMessageRepository;
import com.TTT.TTT.chat.repository.ChatParticipantRepository;
import com.TTT.TTT.chat.repository.ChatRoomRepository;
import com.TTT.TTT.chat.repository.ReadStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;

    public ChatService(ChatRoomRepository chatRoomRepository, ChatParticipantRepository chatParticipantRepository, ChatMessageRepository chatMessageRepository, ReadStatusRepository readStatusRepository, UserRepository userRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.readStatusRepository = readStatusRepository;
        this.userRepository = userRepository;
    }


}
