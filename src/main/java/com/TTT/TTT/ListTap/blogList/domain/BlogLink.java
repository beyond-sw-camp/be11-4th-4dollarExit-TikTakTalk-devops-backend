package com.TTT.TTT.ListTap.blogList.domain;

import com.TTT.TTT.User.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class BlogLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;

    @Column(nullable = false)
    private Integer batch; // 기수 (검색 용)

    @Column(nullable = false)
    private String name; // 유저 이름 (검색 용)

    @Column
    private String nickName;// 유저 닉네임

    @Column(nullable = false, length = 255)
    private String blogUrl;

}
