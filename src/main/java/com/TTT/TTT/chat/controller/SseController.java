package com.TTT.TTT.chat.controller;

import com.TTT.TTT.chat.domain.ChatParticipant;
import com.TTT.TTT.chat.dto.ChatMessageDto;
import com.TTT.TTT.chat.repository.ChatParticipantRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class SseController {
    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    private final ChatParticipantRepository chatParticipantRepository;

    public SseController(ChatParticipantRepository chatParticipantRepository) {
        this.chatParticipantRepository = chatParticipantRepository;
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();
        if (emitterMap.containsKey(loginId)) {
            SseEmitter oldEmitter = emitterMap.get(loginId);
            if (oldEmitter != null) {
                oldEmitter.complete();
            }
            emitterMap.remove(loginId);
        }

        SseEmitter sseEmitter = new SseEmitter(2 * 60 * 1000L); //sse time out 2분으로 설정.
        emitterMap.put(loginId, sseEmitter);
        try {
            sseEmitter.send(SseEmitter.event().name("connect").data("연결완료"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        return sseEmitter;
    }

    @GetMapping("/unsubscribe")
    public void unSubscribe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();
        emitterMap.remove(loginId);
    }

    public void publishMessage(ChatMessageDto chatMessageDto, Long chatRoomId) {
        List<ChatParticipant> offlineUsers = chatParticipantRepository.findByChatRoomIdAndIsConnectedFalse(chatRoomId);

        for (ChatParticipant c : offlineUsers) {
            String loginId = c.getUser().getLoginId();
            SseEmitter sseEmitter = emitterMap.get(loginId);
            if (sseEmitter != null) {
                try {
                    sseEmitter.send(SseEmitter.event().name("chat-message").data(chatMessageDto));
                } catch (IOException e) {
                    e.printStackTrace();
                    emitterMap.remove(loginId);
                }
            }
        }
    }
}



