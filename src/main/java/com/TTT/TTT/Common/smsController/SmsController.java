package com.TTT.TTT.Common.smsController;

import com.TTT.TTT.Common.dtos.AutoCodeDto;
import com.TTT.TTT.Common.smsService.SmsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sms")
public class SmsController {

    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    // 인증번호 전송 API (휴대폰 번호 입력 시 전송)
    @PostMapping("/send-auth")
    public String sendAuthCode(@RequestBody AutoCodeDto dto) {
        System.out.println(dto.getPhoneNumber());
        return smsService.sendAuthCode(dto.getPhoneNumber());
    }

    // 인증번호 검증 API
    @PostMapping("/verify-auth")
    public String verifyAuthCode(@RequestBody AutoCodeDto dto) {
        System.out.println("authCode " + dto.getAuthCode());
        boolean isValid = smsService.verifyAuthCode(dto.getPhoneNumber(), dto.getAuthCode());
        //실패하면 다시 돌아가야하는 메서드 생각해야함.
        return isValid ? "인증 성공!" : "인증 실패!";
    }
}

