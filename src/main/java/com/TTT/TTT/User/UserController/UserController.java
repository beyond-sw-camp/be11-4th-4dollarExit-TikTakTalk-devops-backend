
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
    //유저 회원가입
    @PostMapping("/create")
    public ResponseEntity<?> userCreate(@RequestBody @Valid UserCreateDto dto) {
        userService.userCreate(dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(), "user create successful", "OK"), HttpStatus.CREATED);
    }

    //유저 개인정보조회
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

    //비밀번호 찾기 및 재설정
    @PostMapping("/findPassword")
    public ResponseEntity<?> findUserPw(@RequestBody UserUpdateDto dto) {
        User user = userService.findByLoginId(dto.getLoginId());
        userService.isLoginIdAndPhoneNumberMatches(user.getLoginId(), dto.getPhoneNumber());
        userService.updateUserPassword(dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "success", "ok"),HttpStatus.OK);
    }
    @DeleteMapping("/delete")
    public ResponseEntity<?> userDelete(){
        //로그인 후 추가 예정.
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"user successfully deleted","OK"),HttpStatus.OK);
    }
}
