package com.TTT.TTT.Post.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostUpdateDto {
    @NotBlank(message = "제목을 입력하세요")
    @Size(min = 2, max= 50, message = "제목은 최소 2자 이상 최대 50자까지 가능합니다")
    private String title;

    @NotNull(message = "최소 5자 이상은 입력해야합니다")
    @Size(min = 5, max= 10000, message = "최대 10000자를 넘길 수 없습니다")
    private String contents;
}
