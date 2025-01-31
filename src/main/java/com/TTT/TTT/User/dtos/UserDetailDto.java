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
public class UserDetailDto {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String nickName;
    private String blogLink;
    private String loginId;
    private DelYN delYN;
    private Integer batch;
    private LocalDateTime createdTime;
}
