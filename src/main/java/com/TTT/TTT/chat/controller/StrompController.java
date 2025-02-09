package com.TTT.TTT.chat.controller;

import com.TTT.TTT.chat.dto.ChatMessageDto;
import com.TTT.TTT.chat.service.ChatService;
import com.TTT.TTT.chat.service.RedisPubSubService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

public class StrompController {
    private final SimpMessageSendingOperations messageTemplate;
    private final ChatService chatService;
    private final RedisPubSubService pubSubService;

    public StrompController(SimpMessageSendingOperations messageTemplate, ChatService chatService, RedisPubSubService pubSubService) {
        this.messageTemplate = messageTemplate;
        this.chatService = chatService;
        this.pubSubService = pubSubService;
    }

//    @MessageMapping("/{roomId}")
//    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto chatMessageReqDto) throws JsonProcessingException {
//        System.out.println(chatMessageReqDto.getMessage());
//        chatService.saveMessage(roomId, chatMessageReqDto);
//        chatMessageReqDto.setRoomId(roomId);
//
////        messageTemplate.convertAndSend("/topic/"+roomId, chatMessageReqDto);
//        ObjectMapper objectMapper = new ObjectMapper();
//        String message = objectMapper.writeValueAsString(chatMessageReqDto);
//        pubSubService.publish("chat", message);
//    }
}
