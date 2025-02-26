package com.TTT.TTT.Post.dtos;

import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.Post.domain.Post;
import com.TTT.TTT.User.domain.User;
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
public class PostCreateDto {
    @NotBlank(message = "제목을 입력하세요")
    @Size(min = 2, max= 50, message = "제목은 최소 2자 이상 최대 50자까지 가능합니다")
    private String title;

    @NotNull(message = "최소 5자 이상은 입력해야합니다")
    @Size(min = 5)
    private String contents;
//  화면에서 사용자가 라디오버튼으로 글을 쓸 게시판을 선택하면 해당 게시판의 id값이 넘어오게
    private Long postCategoryId;

    public Post toEntity(User user) {
        return Post.builder()
                .title(this.title)
                .contents(this.contents)
                .user(user)
                .delYN(DelYN.N)
                .build();
    }
}

