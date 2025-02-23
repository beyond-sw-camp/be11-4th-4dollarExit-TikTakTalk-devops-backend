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
        createUserAccounts();
    }
// 데이터 연습 (삭제해야됨)
private void createUserAccounts() {
    String[][] users = {
            {"Alice", "alice@naver.com", "alice1234", "01011112222", "AliceStar", "www.tistory.com/aliceblog", "aliceLogin", "12"},
            {"Bob", "bob@daum.net", "bob5678", "01022223333", "BobTheGreat", "www.medium.com/bobtech", "bobLogin", "13"},
            {"Charlie", "charlie@gmail.com", "charlie9999", "01033334444", "CharlieP", "www.naver.com/charlieworld", "charlieLogin", "14"},
            {"David", "david@kakao.com", "david0000", "01044445555", "DaveC", "www.tistory.com/davidtech", "davidLogin", "15"},
            {"Emma", "emma@outlook.com", "emma4321", "01055556666", "EmmaJ", "www.medium.com/emmadesign", "emmaLogin", "16"},
            {"Frank", "frank@naver.com", "frank9999", "01066667777", "FrankyD", "www.naver.com/frankblog", "frankLogin", "17"},
            {"Grace", "grace@daum.net", "grace5678", "01077778888", "GraceM", "www.medium.com/graceworld", "graceLogin", "18"},
            {"Hank", "hank@gmail.com", "hank2345", "01088889999", "Hankster", "www.tistory.com/hankblog", "hankLogin", "19"},
            {"Ivy", "ivy@kakao.com", "ivy5678", "01099990000", "IvyT", "www.naver.com/ivytech", "ivyLogin", "20"},
            {"Jack", "jack@outlook.com", "jack1234", "01010101111", "JackR", "www.medium.com/jackdesign", "jackLogin", "21"},

            {"Kate", "kate@naver.com", "kate4321", "01011112223", "KateB", "www.tistory.com/kateblog", "kateLogin", "22"},
            {"Leo", "leo@daum.net", "leo5678", "01022223334", "LeoD", "www.medium.com/leotech", "leoLogin", "23"},
            {"Mia", "mia@gmail.com", "mia9999", "01033334445", "MiaX", "www.naver.com/miablog", "miaLogin", "24"},
            {"Nathan", "nathan@kakao.com", "nathan8888", "01044445556", "NateP", "www.tistory.com/nateworld", "nathanLogin", "25"},
            {"Olivia", "olivia@outlook.com", "olivia0000", "01055556667", "OliV", "www.medium.com/oliviadesign", "oliviaLogin", "26"},

            {"Paul", "paul@naver.com", "paul7890", "01066667778", "PaulX", "www.naver.com/paulblog", "paulLogin", "27"},
            {"Quinn", "quinn@daum.net", "quinn3456", "01077778889", "Quinny", "www.medium.com/quinntech", "quinnLogin", "28"},
            {"Ryan", "ryan@gmail.com", "ryan1234", "01088889990", "RyB", "www.tistory.com/ryanworld", "ryanLogin", "29"},
            {"Sophia", "sophia@kakao.com", "sophia9999", "01099990011", "SophT", "www.naver.com/sophiablog", "sophiaLogin", "30"},
            {"Tom", "tom@outlook.com", "tom5678", "01010101112", "TommyG", "www.medium.com/tomdesign", "tomLogin", "31"},

            {"Uma", "uma@naver.com", "uma8765", "01011112224", "UmaC", "www.tistory.com/umaworld", "umaLogin", "32"},
            {"Victor", "victor@daum.net", "victor9876", "01022223335", "VicR", "www.medium.com/victortech", "victorLogin", "33"},
            {"Wendy", "wendy@gmail.com", "wendy3333", "01033334446", "WendyS", "www.naver.com/wendyblog", "wendyLogin", "34"},
            {"Xander", "xander@kakao.com", "xander4444", "01044445557", "XandX", "www.tistory.com/xanderworld", "xanderLogin", "35"},
            {"Yuna", "yuna@outlook.com", "yuna1111", "01055556668", "YunaM", "www.medium.com/yunadesign", "yunaLogin", "36"}
    };

    for (String[] userData : users) {
        String name = userData[0];
        String email = userData[1];
        String password = userData[2];
        String phoneNumber = userData[3];
        String nickName = userData[4];
        String blogLink = userData[5];
        String loginId = userData[6];
        int batch = Integer.parseInt(userData[7]);

        if (!userRepository.findByEmailAndDelYN(email, DelYN.N).isPresent()) {
            User user = User.builder()
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .batch(batch)
                    .role(Role.USER)
                    .nickName(nickName)
                    .blogLink(blogLink)
                    .phoneNumber(phoneNumber)
                    .loginId(loginId)
                    .build();
            userRepository.save(user);
        }
    }
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
