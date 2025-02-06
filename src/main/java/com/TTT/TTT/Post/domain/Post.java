package com.TTT.TTT.Post.domain;

import com.TTT.TTT.Common.BaseTimeEntity;
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
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    private String contents;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DelYN delYn = DelYN.N;

    public void deletePost(){
        this.delYn=DelYN.Y;
    }

    public void update(String title,String contents){
        this.title=title;
        this.contents=contents;
    }


//    private int likes; //좋아요는 아직 생략



}
