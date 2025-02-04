package com.TTT.TTT.User.UserService;

import com.TTT.TTT.User.UserRepository.UserRepository;
import com.TTT.TTT.User.domain.DelYN;
import com.TTT.TTT.User.domain.User;
import com.TTT.TTT.User.dtos.UserCreateDto;
import com.TTT.TTT.User.dtos.UserDetailDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void userCreate(UserCreateDto userCreateDto) throws IllegalArgumentException {
        if (userRepository.findByEmailAndDelYN(userCreateDto.getEmail(), DelYN.N).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        if (userRepository.findByLoginIdAndDelYN(userCreateDto.getLoginId(), DelYN.N).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }

        if (userRepository.findByNickNameAndDelYN(userCreateDto.getNickName(), DelYN.N).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }

        userRepository.save(userCreateDto.toEntity());
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

//    // 아이디 & 전화번호 매칭 확인
//    public boolean validateUser(String phoneNumber, String loginId) {
//        User user = userRepository.findByPhoneNumberAndLoginIdAndDelYN(phoneNumber, loginId,DelYN.N).orElseThrow(
//                ()->new IllegalArgumentException("다시 입력해주세요."));
//        return true;
//    }
//    // 비밀번호 업데이트
//    public void updateUserPassword(String loginId, String newPassword) {
//        User user = userRepository.findByLoginId(loginId);
//        if (user != null) {
//            user.setPassword(passwordEncoder.encode(newPassword));  // 비밀번호 암호화
//            userRepository.save(user);
//        }
//    }
}
