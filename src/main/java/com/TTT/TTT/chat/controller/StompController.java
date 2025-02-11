package com.TTT.TTT.chat.controller;

import com.TTT.TTT.chat.dto.ChatMessageDto;
import com.TTT.TTT.chat.service.ChatService;
import com.TTT.TTT.chat.service.RedisPubSubService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

public class StompController {
    private final SimpMessageSendingOperations messageTemplate;
    private final ChatService chatService;
    private final RedisPubSubService pubSubService;

    public StompController(SimpMessageSendingOperations messageTemplate, ChatService chatService, RedisPubSubService pubSubService) {
        this.messageTemplate = messageTemplate;
        this.chatService = chatService;
        this.pubSubService = pubSubService;
    }

    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto dto) {
        System.out.println(dto.getMessage());
        messageTemplate.convertAndSend("/topic"+roomId, dto);
    }
}
