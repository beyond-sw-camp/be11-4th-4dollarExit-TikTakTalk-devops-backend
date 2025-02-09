package com.TTT.TTT.Post.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostSearchDto {
    private String title;
    private String contents;
}
