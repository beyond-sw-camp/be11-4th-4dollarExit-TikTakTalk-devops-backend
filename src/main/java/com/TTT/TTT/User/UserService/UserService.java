package com.TTT.TTT.User.UserService;

import com.TTT.TTT.Common.smsService.SmsService;
import com.TTT.TTT.User.UserRepository.UserRepository;
import com.TTT.TTT.User.domain.DelYN;
import com.TTT.TTT.User.domain.User;
import com.TTT.TTT.User.dtos.UserCreateDto;
import com.TTT.TTT.User.dtos.UserDetailDto;
import com.TTT.TTT.User.dtos.UserUpdateDto;
import com.TTT.TTT.User.dtos.UserLoginDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsService smsService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, SmsService smsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.smsService = smsService;
    }

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

    public User userLogin(UserLoginDto userLoginDto){
        User user = userRepository.findByLoginIdAndDelYN(userLoginDto.getLoginId(), DelYN.N)
                .orElseThrow(()->new EntityNotFoundException("없는 사용자입니다"));

        if(!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }
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
    // 회원탈퇴
    public void userDelete(){
        //로그인 후 넣을예정
    }


}