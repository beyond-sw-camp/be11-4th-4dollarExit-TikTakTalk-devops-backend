package com.TTT.TTT.chat.controller;

import com.TTT.TTT.chat.dto.ChatMessageDto;
import com.TTT.TTT.chat.service.ChatService;
import com.TTT.TTT.chat.service.RedisPubSubService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class StompController {
    private final SimpMessageSendingOperations messageTemplate;
    private final ChatService chatService;
    private final RedisPubSubService pubSubService;
    private final ObjectMapper objectMapper;

    public StompController(SimpMessageSendingOperations messageTemplate, ChatService chatService, RedisPubSubService pubSubService, ObjectMapper objectMapper) {
        this.messageTemplate = messageTemplate;
        this.chatService = chatService;
        this.pubSubService = pubSubService;
        this.objectMapper = objectMapper;
    }

    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto dto) throws JsonProcessingException {
        chatService.saveMessage(roomId, dto);
        dto.setRoomId(roomId);
        dto.setSendTime(LocalDateTime.now());
        String message = objectMapper.writeValueAsString(dto);
        pubSubService.publish("chat", message);
    }


}
