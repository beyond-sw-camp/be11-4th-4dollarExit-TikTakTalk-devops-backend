package com.TTT.TTT.Comment.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CommentDetailDto {
    private Long commentId;
    private String contents;
    private String profileImageOfCommentAuthor;
    private String nickNameOfCommentAuthor;
    private String loginIdOfCommentAuthor;
    private LocalDateTime createdTime;
    private int rankingPointOfCommentAuthor;
    @Builder.Default
    private List<CommentDetailDto> childCommentList=new ArrayList<>();


    public CommentDetailDto pretendToDelete(){
        this.contents="[삭제된 댓글입니다]";
        return this;
    }



}
