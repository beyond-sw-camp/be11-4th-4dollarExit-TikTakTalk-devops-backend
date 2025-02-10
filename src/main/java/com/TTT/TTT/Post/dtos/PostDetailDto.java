package com.TTT.TTT.Post.dtos;

import com.TTT.TTT.Comment.Dtos.CommentDetailDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostDetailDto {
    private String AuthorNickName;
    private String ProfileImageOfAuthor;
    private int rankingPointOfAuthor;
    private String title;
    private String contents;
    private LocalDateTime createdTime;
    private List<CommentDetailDto> commentList;
    private List<String> attachmentsUrl;
}
