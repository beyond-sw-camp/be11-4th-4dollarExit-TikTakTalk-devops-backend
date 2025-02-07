package com.TTT.TTT.User.domain;

import com.TTT.TTT.Common.Annotation.ForbiddenWords;
import com.TTT.TTT.Common.BaseTimeEntity;
import com.TTT.TTT.Post.domain.Post;
import com.TTT.TTT.User.dtos.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, nullable = false)
    private String name;

//    암호화로 인해 길이 설정 삭제
    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 11, nullable = false)
    private String phoneNumber;     //api 예정

    @Column(nullable = false, unique = true)
    private String nickName;

    // delYN enum 추가
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DelYN delYN = DelYN.N;

    // role 기존 스트링타입을 enum타입으로 교체.
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private Integer batch; //기수

    @Column(length = 50, nullable = false)
    private String blogLink;

    // 로그인아이디 최대 50자로 설정.
    @Column(length = 50, nullable = false, unique = true)
    private String loginId;

    // 랭킹포인트(초기값 0으로 세팅하려고 int사용)
    private int rankingPoint;

    //내가 쓴 글
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Post> myPostList;

    //프로필 사진
    private String profileImagePath;

    public UserDetailDto detailFromEntity(){
        return UserDetailDto.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .nickName(this.nickName)
                .phoneNumber(this.phoneNumber)
                .batch(this.batch)
                .delYN(this.delYN)
                .createdTime(this.getCreatedTime())
                .build();
    }

    public UserMyPageDto myPageFromEntity(){
        return UserMyPageDto.builder()
                .nickName(this.nickName)
                .email(this.email)
                .phoneNumber(this.phoneNumber)
                .batch(this.batch)
                .rankingPoint(this.rankingPoint)
                .blogLink(this.blogLink)
                .profileImage(this.profileImagePath)
                .build();
    }

    public UserListDto ListDtoFromEntity(){
        return UserListDto.builder()
                .loginId(this.getLoginId())
                .name(this.name)
                .email(this.email)
                .phoneNumber(this.phoneNumber)
                .nickName(this.nickName)
                .batch(this.batch)
                .blogLink(this.blogLink)
                .build();
    }

    public UserRankDto RankDtoFromEntity(){
        return UserRankDto.builder()
                .nickName(this.nickName)
                .batch(this.batch)
                .rankingPoint(this.rankingPoint)
                .profileImagePath(this.profileImagePath)
                .build();
    }


//    유저 회원정보 변경
    public void updateUser(UserProfileUpdateDto dto,String newPw){
     if(dto.getNickName() != null){
         this.nickName = dto.getNickName();
     }
     if(dto.getPhoneNumber() != null){
         this.phoneNumber = dto.getPhoneNumber();
     }
     if(dto.getBlogLink() != null){
         this.blogLink = dto.getBlogLink();
     }
     if(newPw != null){
         this.password = newPw;
     }

    }

    public void updateProfileImage(String imagePath){
        this.profileImagePath = imagePath;
    }

    public void userDelete(){
        this.delYN=DelYN.Y;
    }
}
