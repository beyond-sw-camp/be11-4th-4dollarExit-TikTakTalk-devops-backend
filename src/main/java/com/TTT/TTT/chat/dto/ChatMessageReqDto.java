package com.TTT.TTT.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatMessageReqDto {
    private Long roomId;
    private String message;
    private String senderNickname;
}
