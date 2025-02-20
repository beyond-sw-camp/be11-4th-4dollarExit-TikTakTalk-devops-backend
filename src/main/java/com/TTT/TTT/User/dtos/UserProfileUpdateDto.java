package com.TTT.TTT.User.dtos;

//작업 충돌날까봐 업데이트 디티오 따로만들어 작업했습니다

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserProfileUpdateDto {
    private String email;
    private String phoneNumber;
    private String nickName;
    private String blogLink;
    private String newPassword;
}
