package com.TTT.TTT.chat.config;

import com.TTT.TTT.chat.service.ChatService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

@Component
public class StompHandler implements ChannelInterceptor {


    @Value("${jwt.secretKey}")
    private String secretKey;
    private final ChatService chatService;

    public StompHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT == accessor.getCommand()) {
            System.out.println("connect요청시 토큰 유효성 검증");
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            String token = bearerToken.substring(7);
//            토큰 검증
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            System.out.println("토큰 검증 완료");
        }

        if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
            System.out.println("subscribe 검증");
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            String token = bearerToken.substring(7);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String nickName = claims.get("nickName").toString();
            String roomId = accessor.getDestination().split("/")[2];
            accessor.getSessionAttributes().put("nickName", nickName);
            accessor.getSessionAttributes().put("roomId", roomId);
            if (!chatService.isRoomPaticipant(nickName, Long.parseLong(roomId))) {
                throw new AuthenticationServiceException("해당 room에 권한이 없습니다.");
            }
            if (!chatService.getIsconnected(nickName, Long.parseLong(roomId))) {
                chatService.updateUserConnectionStatus(true, nickName, Long.parseLong(roomId));
            }
        }

        if (StompCommand.DISCONNECT == accessor.getCommand()) {
            System.out.println("DISCONNECT 검증");
            Object nickNameObj = accessor.getSessionAttributes().get("nickName");
            Object roomIdObj = accessor.getSessionAttributes().get("roomId");
            if (nickNameObj != null && roomIdObj != null) {
                String nickName = nickNameObj.toString();
                String roomId = roomIdObj.toString();
                chatService.updateUserConnectionStatus(false, nickName, Long.parseLong(roomId));
            }
        }

        return message;
    }
}
