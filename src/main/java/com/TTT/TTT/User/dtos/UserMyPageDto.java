package com.TTT.TTT.User.dtos;

import com.TTT.TTT.User.domain.DelYN;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserMyPageDto {
    private String email;
    private String phoneNumber;
    private String nickName;
    private String blogLink;
    private Integer batch;
    private Integer rankingPoint;
    private String profileImage;
}
