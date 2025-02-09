package com.TTT.TTT.PostCategory.domain;

import com.TTT.TTT.Common.domain.BaseTimeEntity;
import com.TTT.TTT.Post.domain.Post;
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
@Entity
@Builder
public class PostCategory extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String categoryName;
    @OneToMany(mappedBy = "category")
    @Builder.Default
    List<Post> posts = new ArrayList<>();
}
