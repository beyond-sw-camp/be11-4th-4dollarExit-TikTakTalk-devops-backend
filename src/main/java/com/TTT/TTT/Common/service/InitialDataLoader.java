package com.TTT.TTT.Common.service;

import com.TTT.TTT.PostCategory.Repository.PostCategoryRepository;
import com.TTT.TTT.PostCategory.domain.PostCategory;
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
    private final PostCategoryRepository postCategoryRepository;
    public InitialDataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder, PostCategoryRepository postCategoryRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.postCategoryRepository = postCategoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        createAdminAccount();
        craeteUserAccount();
        createCategories();

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
                    .batch(11)
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

//    프로그램실행하면 기본게시판 만들어두는 것(테스트 편하게 하기 위해 등록)
    private void createCategories(){
        if(!postCategoryRepository.findByCategoryName("자유게시판").isPresent()){
             PostCategory free = PostCategory.builder()
                .categoryName("자유게시판")
                .build();
             postCategoryRepository.save(free);
    }
        if(!postCategoryRepository.findByCategoryName("정보게시판").isPresent()){
            PostCategory information = PostCategory.builder()
                    .categoryName("정보게시판")
                    .build();
            postCategoryRepository.save(information);
        }

        if(!postCategoryRepository.findByCategoryName("알고리즘게시판").isPresent()){
            PostCategory information = PostCategory.builder()
                    .categoryName("알고리즘게시판")
                    .build();
            postCategoryRepository.save(information);
        }
    }
}
