package com.TTT.TTT.Post.domain;

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
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500) // 대략 500자 설정
    private String contents;

    private LocalDateTime createdTime;

    public void update(String title,String contents){
        this.title=title;
        this.contents=contents;
    }

    public void getContent() {
    }

// DelYn  추가 필요
//    private int likes; //좋아요는 아직 생략



}
