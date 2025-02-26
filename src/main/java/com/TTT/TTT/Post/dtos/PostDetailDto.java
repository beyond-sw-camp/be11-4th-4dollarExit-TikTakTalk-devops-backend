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
    private Long postId;
    private Long postUserId;
    private String authorId;
    private String authorNickName;
    private String AuthorImage;
    private int rankingPointOfAuthor;
    private String title;
    private String contents;
    private LocalDateTime createdTime;
    private int likesCount;
    private int viewCount;
    private boolean liked;
    private String categoryName;
    private List<CommentDetailDto> commentList;
    private List<String> attachmentsUrl;
}
