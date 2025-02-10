package com.TTT.TTT.Comment.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommentUpdateDto {
    @NotBlank(message = "최소 2자이상 최대 1000자 이하의 글자수를 입력해야합니다.")
    @Size(message = "최소 2자이상 최대 1000자 이하의 글자수를 입력해야합니다")
    private String contents;
}
