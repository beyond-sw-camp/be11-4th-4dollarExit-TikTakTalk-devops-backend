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
        @NotNull
        private Long postId;
//      댓글은 여기가 null값, 대댓글이라면 숫자값을 가질 것이다.
        private Long parentId;



}
