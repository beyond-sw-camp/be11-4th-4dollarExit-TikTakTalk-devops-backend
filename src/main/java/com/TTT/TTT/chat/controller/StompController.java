package com.TTT.TTT.chat.controller;

import com.TTT.TTT.chat.domain.ChatParticipant;
import com.TTT.TTT.chat.domain.ChatRoom;
import com.TTT.TTT.chat.dto.ChatMessageDto;
import com.TTT.TTT.chat.repository.ChatParticipantRepository;
import com.TTT.TTT.chat.repository.ChatRoomRepository;
import com.TTT.TTT.chat.service.ChatService;
import com.TTT.TTT.chat.service.RedisPubSubService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class StompController {
    private final ChatService chatService;
    private final RedisPubSubService pubSubService;
    private final ObjectMapper objectMapper;
    private final SseController sseController;
    private final ChatParticipantRepository chatParticipantRepository;

    public StompController(ChatService chatService, RedisPubSubService pubSubService, ObjectMapper objectMapper, SseController sseController, ChatParticipantRepository chatParticipantRepository) {
        this.chatService = chatService;
        this.pubSubService = pubSubService;
        this.objectMapper = objectMapper;
        this.sseController = sseController;
        this.chatParticipantRepository = chatParticipantRepository;
    }

    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto dto) throws JsonProcessingException {
        chatService.saveMessage(roomId, dto);
        dto.setRoomId(roomId);
        dto.setSendTime(LocalDateTime.now());
        String message = objectMapper.writeValueAsString(dto);
        pubSubService.publish("chat", message);
        List<ChatParticipant> unConnectioned = chatParticipantRepository.findByChatRoomIdAndIsConnectedFalse(roomId);
        for (ChatParticipant c : unConnectioned) {
            pubSubService.publish("sse-chat", message);
        }
    }


}
