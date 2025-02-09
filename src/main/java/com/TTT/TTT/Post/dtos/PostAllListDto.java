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
public class PostAllListDto {
    private String title;
    private LocalDateTime createdTime;
    private String AuthorNickName;
    private String AuthorImage;
    //  댓글 안달리면 0개 초기값 주기 위해 int로 설정
    private int countOfComment;
    private String categoryName;
}
