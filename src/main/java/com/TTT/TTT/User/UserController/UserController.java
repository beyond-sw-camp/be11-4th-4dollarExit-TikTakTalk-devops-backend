
package com.TTT.TTT.User.UserController;

import com.TTT.TTT.Common.dtos.CommonDto;
import com.TTT.TTT.Common.auth.JwtTokenProvider;
import com.TTT.TTT.Post.domain.Post;
import com.TTT.TTT.User.UserService.UserService;
import com.TTT.TTT.User.domain.User;
import com.TTT.TTT.User.dtos.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.HashMap;
import java.util.List;
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

//   1.회원가입
    @PostMapping("/create")
    public ResponseEntity<?> userCreate(@RequestBody @Valid UserCreateDto dto) {
        userService.userCreate(dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(), "user create successful", "OK"), HttpStatus.CREATED);
    }
//  2.로그인
    @PostMapping("/login")
    public ResponseEntity<?> userLogin(@RequestBody UserLoginDto userLoginDto){
//        loginId, password 검증 (로그인 시도 횟수 추가해야함)
        User user = userService.userLogin(userLoginDto);

//        일치할 경우 access 토큰 발행 및 userId, token 리턴
        String jwtToken = jwtTokenProvider.createToken(user.getLoginId(), user.getRole().toString(), user.getNickName());
//        refresh 토큰도 발행
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getLoginId(),user.getRole().toString(), user.getNickName());
        redisTemplate.opsForValue().set(user.getLoginId(), refreshToken, 200, TimeUnit.DAYS); //레디스db에 키값으로 로그인 아이디, value로 토큰값을 넣겠다. 그리고 200일지나면 삭제하도록 설정

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", user.getId());
        loginInfo.put("token", jwtToken);
        loginInfo.put("refreshToken", refreshToken);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "login sucess",loginInfo),HttpStatus.OK);
    }

//    3.내 정보 조회
    @GetMapping("/myInformation")
    public ResponseEntity<?> myInfo(){
        UserMyPageDto userMyPageDto = userService.myInfo();
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"myPage upload success",userMyPageDto),HttpStatus.OK);
    }

//    4.내가 쓴 게시글 조회(**포스트 쪽 들어오면 포스트아닌 조회용DTO리턴하도록 수정 계획)
    @GetMapping("/myPostList")
    public ResponseEntity<?> myPostList(){
        List<Post> myPostList = userService.myPostList();
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "my postList upload sueccess",myPostList),HttpStatus.OK);
    }

//    5. 내 정보 수정(휴대폰인증 완료되면 수정 or 삭제 계획)
    @PatchMapping("/updateProfile")
    public ResponseEntity<?> updateProfile(@RequestBody UserProfileUpdateDto userProfileUpdateDto){
         userService.updateProfile(userProfileUpdateDto);
         return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "user information update success","success"),HttpStatus.OK);
    }

//    6.프로필 이미지 수정
    @PatchMapping("/changeProfileImage")
    public ResponseEntity<?> updateProfileImage(MultipartFile image){
        userService.updateProfileImage(image);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"profile image is changed successfully","success"),HttpStatus.OK);
    }

//    7.회원 목록 조회
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> findAllUser(@PageableDefault(size = 20) Pageable pageable){
        Page<UserListDto> list =  userService.findAll(pageable);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"userList is uploaded successfully",list),HttpStatus.OK);
    }

//    8.유저 상위 랭킹 5명 조회
    @GetMapping("/rankingfive")
    public ResponseEntity<?> userRanking(){
         List<UserRankDto> userRank =  userService.userRanking();
         return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"userRankList is uploaded successfully",userRank),HttpStatus.OK);
    }
//    9.회원탈퇴
    @DeleteMapping("/deleteMember")
    public ResponseEntity<?> userDelete(){
             userService.userDelete();
             return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"deletion success","success"),HttpStatus.OK);
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
  

  

//** 리프레시 토큰 발급
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

        String token = jwtTokenProvider.createRefreshToken(claims.getSubject(),claims.get("role").toString(), claims.get("nickName").toString());
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("token",token);
        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(), "accessToken is recreated",loginInfo),HttpStatus.CREATED);
    }
}
