package com.TTT.TTT.User.UserController;

import com.TTT.TTT.Common.CommonDto;
import com.TTT.TTT.Common.auth.JwtTokenProvider;
import com.TTT.TTT.User.UserService.UserService;
import com.TTT.TTT.User.domain.User;
import com.TTT.TTT.User.dtos.UserCreateDto;
import com.TTT.TTT.User.dtos.UserDetailDto;
import com.TTT.TTT.User.dtos.UserLoginDto;
import com.TTT.TTT.User.dtos.UserRefreshDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/ttt/user")
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    @Qualifier("rtdb")
    private final RedisTemplate<String,Object> redisTemplate;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider, @Qualifier("rtdb") RedisTemplate<String, Object> redisTemplate) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

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
//        refresh 토큰도 발행
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getLoginId(),user.getRole().toString());
        redisTemplate.opsForValue().set(user.getLoginId(), refreshToken, 200, TimeUnit.DAYS); //레디스db에 키값으로 로그인 아이디, value로 토큰값을 넣겠다. 그리고 200일지나면 삭제하도록 설정

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", user.getId());
        loginInfo.put("token", jwtToken);
        loginInfo.put("refreshToken", refreshToken);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "login sucess",loginInfo),HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
//   사용자가 만료된 AccessToken을 갱신할 때 호출하여 레디스에서 사용자의 RefreshToken을 검증하고 새로운 AccessToken을 지급
    public ResponseEntity<?> reGenerateAccessToken(@RequestBody UserRefreshDto dto){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKeyRt)
                .build()
                .parseClaimsJws(dto.getRefreshToken())
                .getBody();

        Object refreshTokenOfDto  = redisTemplate.opsForValue().get(claims.getSubject());//레디스에서 키값(loginId)에 해당하는 밸류 가지고옴
        if(refreshTokenOfDto == null || !refreshTokenOfDto.toString().equals(dto.getRefreshToken())){//레디스에 해당 키가 없거나 레디스에 있는 값과 일치하지 않으면 accessToken재발급 불가
            return new ResponseEntity<>(new CommonDto(HttpStatus.BAD_REQUEST.value(), "cannot recreate accessToken",null),HttpStatus.BAD_REQUEST);
        }

        String token = jwtTokenProvider.createRefreshToken(claims.getSubject(),claims.get("role").toString());
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("token",token);
        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(), "accessToken is recreated",loginInfo),HttpStatus.CREATED);
    }
}
