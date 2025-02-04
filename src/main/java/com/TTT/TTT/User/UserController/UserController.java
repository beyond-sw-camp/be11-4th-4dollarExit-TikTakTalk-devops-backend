package com.TTT.TTT.User.UserController;

import com.TTT.TTT.Common.CommonDto;
import com.TTT.TTT.User.UserService.UserService;
import com.TTT.TTT.User.domain.User;
import com.TTT.TTT.User.dtos.UserCreateDto;
import com.TTT.TTT.User.dtos.UserDetailDto;
import com.TTT.TTT.User.dtos.UserFindDto;
import com.TTT.TTT.User.dtos.UserUpdateDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ttt/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> userCreate(@RequestBody @Valid UserCreateDto dto) {
        userService.userCreate(dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(), "user create successful", "OK"), HttpStatus.CREATED);
    }
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> userDetail(@PathVariable Long id) {
        UserDetailDto userDetailDto = userService.findById(id);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "User found",userDetailDto),HttpStatus.OK);
    }
    //아이디 찾기
    @PostMapping("/findLoginId")
    public ResponseEntity<?> findLoginId(@RequestBody UserFindDto dto){
        String loginId = userService.findLoginIdByPhoneNumber(dto.getPhoneNumber());
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "success",loginId),HttpStatus.OK);
    }
//    //비밀번호 찾기 및 재설정
//    @PostMapping("/findUserPw")
//    public ResponseEntity<?> findUserPw(@RequestBody UserUpdateDto dto) {
//        boolean isUserValid = userService.validateUser(dto.getPhoneNumber(), dto.getLoginId());
//
//        if (!isUserValid) {
//            return new ResponseEntity<>(new CommonDto(HttpStatus.NOT_FOUND.value()
//                    ,"사용자를 찾을 수 없습니다.",!isUserValid),HttpStatus.NOT_FOUND);
//        }
//
//        // 비밀번호 업데이트
//        userService.updateUserPassword(dto.getLoginId(), dto.getNewPassword());
//        return ResponseEntity.ok(new CommonDto("success", "비밀번호가 변경되었습니다."));
//    }


}
