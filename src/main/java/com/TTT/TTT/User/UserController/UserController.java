package com.TTT.TTT.User.UserController;

import com.TTT.TTT.Common.CommonDto;
import com.TTT.TTT.User.UserService.UserService;
import com.TTT.TTT.User.domain.User;
import com.TTT.TTT.User.dtos.UserCreateDto;
import com.TTT.TTT.User.dtos.UserDetailDto;
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
    public ResponseEntity<?> userDetail(@PathVariable Long id ,UserDetailDto dto) {
        UserDetailDto userDetailDto = userService.findById(id);
        return ResponseEntity.ok(new CommonDto(HttpStatus.OK.value(), "User found", userDetailDto));
    }


}
