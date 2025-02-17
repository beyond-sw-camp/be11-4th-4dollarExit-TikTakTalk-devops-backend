package com.TTT.TTT.ListTap.blogList.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BlogLinkResponseDto {
    private Long blogId;
    private Long userId;
    private String name;
    private Integer batch;
    private String blogUrl;
}
