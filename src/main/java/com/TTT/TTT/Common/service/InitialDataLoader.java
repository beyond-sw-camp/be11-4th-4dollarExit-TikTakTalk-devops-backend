package com.TTT.TTT.Common.service;

import com.TTT.TTT.User.UserRepository.UserRepository;
import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.User.domain.Role;
import com.TTT.TTT.User.domain.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

//테스트용 admin계정 및 일반계정 자동생성.
@Component
public class InitialDataLoader implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialDataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        createAdminAccount();
        craeteUserAccount();
    }

    private void createAdminAccount() {
//        이메일만 체크하는 이유는 서버가 실행될때 바로 DB에 저장되므로,
//        admi다른 검증은 필요하지 않은것으로 보임.
//        if문 검증을 안할시 서버시작 할 때 에러발생.
        if (!userRepository.findByEmailAndDelYN("admin@naver.com", DelYN.N).isPresent()) {
            User user = User.builder()
                    .name("joonhyuk")
                    .password(passwordEncoder.encode("12341234"))
                    .email("admin@naver.com")
                    .phoneNumber("01012341234")
                    .nickName("Ganzi")
                    .role(Role.ADMIN)
                    .loginId("admin")
                    .build();
            userRepository.save(user);
        }
    }

    private void craeteUserAccount() {
        if (!userRepository.findByEmailAndDelYN("user@naver.com", DelYN.N).isPresent()) {
            User user = User.builder()
                    .name("joonhyuk")
                    .email("user@naver.com")
                    .password(passwordEncoder.encode("12341234"))
                    .batch(11)
                    .role(Role.USER)
                    .nickName("NoGanzi")
                    .blogLink("www.naver.NoGanzi's blog")
                    .phoneNumber("01012341234")
                    .loginId("user")
                    .build();
            userRepository.save(user);
        }
    }
}
