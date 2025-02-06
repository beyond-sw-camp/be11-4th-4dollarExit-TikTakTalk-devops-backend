package com.TTT.TTT.Post.dtos;

import com.TTT.TTT.Post.domain.DelYN;
import com.TTT.TTT.Post.domain.Post;
import com.TTT.TTT.User.domain.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostSaveReq {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    public Post toEntity(User user) {
        return Post.builder()
                .title(this.title)
                .contents(this.content)
                .user(user)
                .delYn(DelYN.N)
                .build();
    }
}

