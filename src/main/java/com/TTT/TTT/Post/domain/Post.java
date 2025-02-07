package com.TTT.TTT.Post.domain;

import com.TTT.TTT.Common.BaseTimeEntity;
import com.TTT.TTT.User.domain.DelYN;
import com.TTT.TTT.User.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(name = "post")
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    private String contents;

    private LocalDateTime createdTime;

    @Enumerated(EnumType.STRING)
    private DelYN delYn;

    public void update(String title,String contents){
        this.title=title;
        this.contents=contents;
    }

// BaseimeEntity 상속
// DelYn  추가 필요
//    private int likes; //좋아요는 아직 생략



}