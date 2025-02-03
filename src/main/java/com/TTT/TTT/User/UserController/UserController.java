package com.TTT.TTT.User.UserController;

import com.TTT.TTT.Common.CommonDto;
import com.TTT.TTT.Common.auth.JwtTokenProvider;
import com.TTT.TTT.User.UserService.UserService;
import com.TTT.TTT.User.domain.User;
import com.TTT.TTT.User.dtos.UserCreateDto;
import com.TTT.TTT.User.dtos.UserDetailDto;
import com.TTT.TTT.User.dtos.UserLoginDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ttt/user")
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/create")
    public ResponseEntity<?> userCreate(@RequestBody @Valid UserCreateDto dto) {
        userService.userCreate(dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(), "user create successful", "OK"), HttpStatus.CREATED);
    }
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> userDetail(@PathVariable Long id ,UserDetailDto dto) {
        UserDetailDto userDetailDto = userService.findById(id);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "User found",userDetailDto),HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> userLogin(@RequestBody UserLoginDto userLoginDto){
//        loginId, password 검증 (로그인 시도 횟수 추가해야함)
        User user = userService.userLogin(userLoginDto);

//        일치할 경우 access 토큰 발행 및 userId, token 리턴
        String jwtToken = jwtTokenProvider.createToken(user.getLoginId(), user.getRole().toString());
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", user.getId());
        loginInfo.put("token", jwtToken);
        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }
}
