package com.TTT.TTT.Post.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostListDto {
    private Long postId;
    private String title;
    private String contents;
    private LocalDateTime createdTime;
    private String AuthorNickName;
    private String AuthorImage;
//  댓글 안달리면 0개 초기값 주기 위해 int로 설정
    private int countOfComment;
    private int likesCount;
    private int viewCount;
    private int AuthorRankingPoint;
}
