package com.TTT.TTT.ListTap.blogList.domain;

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


    @Column(nullable = false)
    private Integer batch; // 기수 (검색 용)

    @Column(nullable = false)
    private String name; // 유저 이름 (검색 용)

    @Column
    private String phoneNumber;// 유저 전화번호

    @Column(nullable = false, length = 255)
    private String blogUrl;

}
