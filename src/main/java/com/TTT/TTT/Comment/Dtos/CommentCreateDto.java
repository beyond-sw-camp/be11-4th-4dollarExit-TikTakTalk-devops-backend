package com.TTT.TTT.Comment.Dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommentCreateDto {
        @NotNull
        private String contents;
//      포스트 id값을 가지면 post에 대한 댓글 project id값을 가지면 project에 대한 댓글.
        private Long postId;
        private Long projectId;

//      댓글은 여기가 null값, 대댓글이라면 숫자값을 가질 것이다.
        private Long parentId;



}
