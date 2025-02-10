package com.TTT.TTT.User.UserService;

//import com.TTT.TTT.Common.smsService.SmsService;
import com.TTT.TTT.Common.smsService.SmsService;
import com.TTT.TTT.Post.domain.Post;
//import com.TTT.TTT.Common.smsService.SmsService;
import com.TTT.TTT.User.UserRepository.UserRepository;
import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.User.domain.User;
import com.TTT.TTT.User.dtos.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Client s3Client;
    private final SmsService smsService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, S3Client s3Client, SmsService smsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.s3Client = s3Client;
        this.smsService = smsService;
    }
  
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

// 1.회원가입
    public void userCreate(UserCreateDto userCreateDto) throws IllegalArgumentException {
//        if (!smsService.verifyAuthCode(userCreateDto.getPhoneNumber(), userCreateDto.getPhoneNumberInput())) {
//            throw new IllegalArgumentException("휴대폰 인증이 완료되지 않았습니다.");
//        } 휴대폰 검증 로직
        if (userRepository.findByLoginIdAndDelYN(userCreateDto.getLoginId(), DelYN.N).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }
        if (userRepository.findByNickNameAndDelYN(userCreateDto.getNickName(), DelYN.N).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }
        if (userRepository.findByEmailAndDelYN(userCreateDto.getEmail(), DelYN.N).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
        //휴대폰검증로직
//        String phoneNumder = userCreateDto.getPhoneNumber();
//        String inputNumber = userCreateDto.getPhoneNumberInput();
//        smsService.sendAuthCode(phoneNumder);
//        smsService.verifyAuthCode(phoneNumder,inputNumber);

        userRepository.save(userCreateDto.toEntity(passwordEncoder.encode(userCreateDto.getPassword())));
    }
// 2.로그인
    public User userLogin(UserLoginDto userLoginDto){
        User user = userRepository.findByLoginIdAndDelYN(userLoginDto.getLoginId(), DelYN.N)
                .orElseThrow(()->new EntityNotFoundException("없는 사용자입니다"));

        if(!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }
// 3. 내 정보 조회
    public UserMyPageDto myInfo(){
       String userLogin =  SecurityContextHolder.getContext().getAuthentication().getName();
       User user = userRepository.findByLoginIdAndDelYN(userLogin,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 아이디입니다"));
       return user.myPageFromEntity();
    }

// 4. 내가 쓴 게시글 조회
    public List<Post> myPostList(){
      String userLogin =  SecurityContextHolder.getContext().getAuthentication().getName();
      User user = userRepository.findByLoginIdAndDelYN(userLogin,DelYN.N).orElseThrow(()-> new EntityNotFoundException("없는 아이디입니다"));
      return user.getMyPostList();
    }

//  5. 내 정보 수정
    public void updateProfile(UserProfileUpdateDto dto){
          String userLogin = SecurityContextHolder.getContext().getAuthentication().getName();
          User user = userRepository.findByLoginIdAndDelYN(userLogin,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 아이디입니다"));
          String newPw = null;
          if(dto.getNewPassword() != null){
             newPw = passwordEncoder.encode(dto.getNewPassword());
          }
          user.updateUser(dto,newPw);
    }

//    6.내 프로필 이미지 수정
    public void updateProfileImage(MultipartFile image){
       try {
           Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
           User user = userRepository.findByLoginIdAndDelYN(authentication.getName(), DelYN.N).orElseThrow(() -> new EntityNotFoundException("없는 아이디입니다"));
           //이미지 일단 로컬에 저장하기 위해  이미지를 바이트배열로 바꿈
//          MultipartFile image = dto.getImage();
           byte[] bytes = image.getBytes();
           String fileName = user.getId() + "_" + image.getOriginalFilename();
           //로컬에 저장. 수업시간때 썼던 폴더 경로라 다들 같으실 겁니다.
           Path path = Paths.get("C:/Users/Playdata/Desktop/tmp/", fileName);
           Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            //aws에 저장
           PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                   .bucket(bucket)
                   .key(fileName)
                   .build();
           s3Client.putObject(putObjectRequest, RequestBody.fromFile(path));
           //aws로 부터 url경로 받아옴
           String s3Url = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(fileName)).toExternalForm();
           user.updateProfileImage(s3Url);
       } catch (IOException e){
           throw new RuntimeException("이미지 저장 실패"); //
       }
    }

//    7.회원 목록 조회
    public Page<UserListDto> findAll(Pageable pageable){
        Page<User> userList = userRepository.findAll(pageable);
      return userList.map(u->u.ListDtoFromEntity());
    }

//    8.유저 랭킹 조회
    public List<UserRankDto> userRanking(){
        List<User> rankfive = userRepository.findTop5ByOrderByRankingPointDesc();
       return rankfive.stream().map(u->u.RankDtoFromEntity()).toList();

    }

//    9.유저 삭제
    public void userDelete(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByLoginIdAndDelYN(authentication.getName(), DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 아이디입니다"));
        user.userDelete();
}







    //유저 개인 정보 조회
    public UserDetailDto findById(Long id){
        return userRepository.findByIdAndDelYN(id,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 아이디입니다")).detailFromEntity();
    }

    // 전화번호로 아이디 찾기
    public String findLoginIdByPhoneNumber(String phoneNumber) {
        User user = userRepository.findByPhoneNumberAndDelYN(phoneNumber,DelYN.N).orElseThrow(
                ()->new EntityNotFoundException("없는 번호입니다."));
        return user.getLoginId();
    }

    //  비밀번호 찾기
    // 1.아이디 확인
    public User findByLoginId(String loginId){
        return userRepository.findByLoginIdAndDelYN(loginId, DelYN.N).orElseThrow(() -> new IllegalArgumentException("없는 사용자입니다"));
    }
    // 2. 휴대폰번호 확인.
    public void isLoginIdAndPhoneNumberMatches(String loginId, String phoneNumber){
        if(!findByLoginId(loginId).getPhoneNumber().equals(phoneNumber)){
            throw new IllegalArgumentException("전화번호가 일치하지 않습니다.");
        }
    }
    // 3. 비밀번호 재설정.
    public void updateUserPassword(UserUpdateDto dto){
        User user = userRepository.findByLoginIdAndDelYN(dto.getLoginId(), DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 사용자입니다"));
        // 아직 로그인 안넣었음 .
//        user.updateUserPassword(passwordEncoder.encode(dto.getNewPassword()));
    }



}