package com.TTT.TTT.Common.smsController;

import com.TTT.TTT.Common.dtos.AutoCodeDto;
import com.TTT.TTT.Common.smsService.SmsService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> verifyAuthCode(@RequestBody AutoCodeDto dto) {
        System.out.println("authCode " + dto.getAuthCode());
        boolean isValid = smsService.verifyAuthCode(dto.getPhoneNumber(), dto.getAuthCode());
        if (isValid) {
            return ResponseEntity.ok("인증 성공!"); // 200 OK
        } else {
            return ResponseEntity.badRequest().body("인증 실패!"); // 400 Bad Request
        }
    }
}

