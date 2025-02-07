package com.TTT.TTT.Post.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostDetailRes {
    private Long id;
    private String title;
    private String content;
    private String createdTime;
}
