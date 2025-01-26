package com.TTT.TTT.User.domain;

import com.TTT.TTT.Common.Annotation.ForbiddenWords;
import com.TTT.TTT.Common.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(length = 20, nullable = false)
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

    @Column(length = 20, nullable = false)
    private String blogLink;

    // 로그인아이디 최대 50자로 설정.
    @Column(length = 50, nullable = false, unique = true)
    private String loginId;
}
