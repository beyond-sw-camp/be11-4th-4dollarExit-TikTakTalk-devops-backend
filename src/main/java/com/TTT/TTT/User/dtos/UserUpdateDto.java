package com.TTT.TTT.User.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserUpdateDto {
    private String loginId;
    private String phoneNumber;
    private String newPassword;  //비밀번호 재설정 용도

}
