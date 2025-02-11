package com.TTT.TTT.Likes.domain;

import com.TTT.TTT.Common.domain.BaseTimeEntity;
import com.TTT.TTT.Post.domain.Post;
import com.TTT.TTT.User.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Likes extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId")
    Post post;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    User user;


}
