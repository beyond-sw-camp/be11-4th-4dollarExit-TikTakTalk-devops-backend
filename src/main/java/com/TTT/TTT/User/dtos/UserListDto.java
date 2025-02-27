package com.TTT.TTT.User.dtos;

import com.TTT.TTT.Common.domain.DelYN;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserListDto {
    private String loginId;
    private String name;
    private String email;
    private String phoneNumber;
    private String nickName;
    private Integer batch;
    private String blogLink;
    private DelYN delYN;
    private int rankingPoint;
}
