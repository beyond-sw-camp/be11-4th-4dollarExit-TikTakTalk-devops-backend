package com.TTT.TTT.User.domain;

import com.TTT.TTT.Common.Annotation.ForbiddenWords;
import com.TTT.TTT.Common.domain.BaseTimeEntity;
import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.Likes.domain.Likes;
import com.TTT.TTT.ListTap.blogList.dtos.BlogLinkResponseDto;
import com.TTT.TTT.Post.domain.Post;
import com.TTT.TTT.User.dtos.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String nickName;

    // delYN enum 추가
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DelYN delYN = DelYN.N;

    // role 기존 스트링타입을 enum타입으로 교체.
    @Enumerated(EnumType.STRING)
    private Role role;

//    DTO에서 Notnull로 잡으므로 InitialDetaLoader Admin계정 추가를 위해 nullable = false 삭제
    private Integer batch; //기수

//    batch와 마찬가지로 nullable 조건 삭제
    @Column(length = 50)
    private String blogLink;

    // 로그인아이디 최대 50자로 설정.
    @Column(length = 50, nullable = false, unique = true)
    private String loginId;

    // 랭킹포인트(초기값 0으로 세팅하려고 int사용)
    private int rankingPoint;

    //내가 쓴 글
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Post> myPostList;

    //내가 좋아요한 목록
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Likes> myLikes = new ArrayList<>();

//    로그인 타입
    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private String socialId;

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
                .rankingPoint(this.rankingPoint)
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

//    블로그 링크 를 위해
    public BlogLinkResponseDto  toBlogDto(){
        return BlogLinkResponseDto.builder()
                .userId(this.id)
                .blogUrl(this.blogLink)
                .batch(this.batch)
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

    public void rankingPointUpdate(int point){
        this.rankingPoint += point;
    }

    public void addLikeInMyLikes(Likes likes){
        this.myLikes.add(likes);
    }
}
