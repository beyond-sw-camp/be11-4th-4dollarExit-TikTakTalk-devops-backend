package com.TTT.TTT.PostCategory.Service;

import com.TTT.TTT.PostCategory.Dtos.PostCategoryListDto;
import com.TTT.TTT.PostCategory.Repository.PostCategoryRepository;
import com.TTT.TTT.PostCategory.domain.PostCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PostCategoryService {

    private final PostCategoryRepository postCategoryRepository;


    public PostCategoryService(PostCategoryRepository postCategoryRepository) {
        this.postCategoryRepository = postCategoryRepository;
    }

//    1.카테고리 리스트 조회
    public List<PostCategoryListDto> findAll(){
        List<PostCategory> categories = postCategoryRepository.findAll();
        return categories.stream().map(c->c.toDto()).toList();
    }
}
