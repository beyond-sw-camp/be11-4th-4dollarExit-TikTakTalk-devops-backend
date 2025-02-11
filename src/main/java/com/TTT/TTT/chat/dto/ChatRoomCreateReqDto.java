package com.TTT.TTT.chat.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomCreateReqDto {
    @Column(unique = true, length = 20)
    @Size(min = 2, max = 20, message = "방제목은 최소2글자 최대 20글자입니다.")
    private String roomName;
}
