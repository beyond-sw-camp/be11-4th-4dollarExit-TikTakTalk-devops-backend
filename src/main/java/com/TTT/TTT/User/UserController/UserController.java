package com.TTT.TTT.User.UserController;

import com.TTT.TTT.Common.CommonDto;
import com.TTT.TTT.User.UserService.UserService;
import com.TTT.TTT.User.domain.User;
import com.TTT.TTT.User.dtos.UserCreateDto;
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
    @PostMapping("/detail/{id}")
    public ResponseEntity<?> userDetail(@PathVariable Long id) {
        userService.findById(id);
        return new ResponseEntity<>(new CommonDto(HttpStatus.NOT_FOUND.value(), "not found","OK"),HttpStatus.NOT_FOUND);
    }


}
