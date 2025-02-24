package com.TTT.TTT.chat.service;

import com.TTT.TTT.chat.controller.SseController;
import com.TTT.TTT.chat.dto.ChatMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
public class RedisPubSubService implements MessageListener {

    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessageSendingOperations messageTemplate;
    private final ObjectMapper objectMapper;
    private final SseController sseController;

    public RedisPubSubService(@Qualifier("PubSub") StringRedisTemplate stringRedisTemplate, SimpMessageSendingOperations messageTemplate, ObjectMapper objectMapper, SseController sseController) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.messageTemplate = messageTemplate;
        this.objectMapper = objectMapper;
        this.sseController = sseController;
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void publish(String channel, String message){
        stringRedisTemplate.convertAndSend(channel, message);
    }

    @Override
//    pattern에는 topic의 이름의 패턴이 담겨있고, 이 패턴을 기반으로 다이나믹한 코딩
    public void onMessage(Message message, byte[] pattern) {
        String payload = new String(message.getBody());
        String channel = new String(pattern);
        try {
            ChatMessageDto chatMessageDto = objectMapper.readValue(payload, ChatMessageDto.class);

            if ("chat".equals(channel)) {
                messageTemplate.convertAndSend("/topic/" + chatMessageDto.getRoomId(), chatMessageDto);
            }

            if ("sse-chat".equals(channel)) {
                sseController.publishMessage(chatMessageDto, chatMessageDto.getRoomId());
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
