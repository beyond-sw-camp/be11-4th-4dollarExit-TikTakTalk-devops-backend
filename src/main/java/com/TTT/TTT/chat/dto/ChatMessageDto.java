package com.TTT.TTT.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatMessageDto {
    private Long roomId;
    private String message;
    private String senderNickName;
}
