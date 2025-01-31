package com.TTT.TTT.User.UserService;

import com.TTT.TTT.User.UserRepository.UserRepository;
import com.TTT.TTT.User.domain.DelYN;
import com.TTT.TTT.User.domain.User;
import com.TTT.TTT.User.dtos.UserCreateDto;
import com.TTT.TTT.User.dtos.UserDetailDto;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

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
        return userRepository.findByIdAndDelYN(id,DelYN.N).orElseThrow(()->new IllegalArgumentException("없는 아이디입니다")).detailFromEntity();
    }
}
