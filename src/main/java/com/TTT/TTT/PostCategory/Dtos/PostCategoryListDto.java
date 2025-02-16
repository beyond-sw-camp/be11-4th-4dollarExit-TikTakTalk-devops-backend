package com.TTT.TTT.PostCategory.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostCategoryListDto {
    private Long categoryId;
    private String categoryName;
}
