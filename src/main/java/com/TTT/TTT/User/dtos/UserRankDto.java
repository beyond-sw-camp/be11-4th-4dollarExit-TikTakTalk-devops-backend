package com.TTT.TTT.User.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserRankDto {
    private String nickName;
    private Integer batch;
    private int rankingPoint;
    private String profileImagePath;
}
