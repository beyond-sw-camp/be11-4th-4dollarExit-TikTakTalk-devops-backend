package com.TTT.TTT.PostCategory.Service;

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
    public List<String> findAll(){
        List<PostCategory> categories = postCategoryRepository.findAll();
        List<String> categoryList = new ArrayList<>();
        for(PostCategory c : categories){
           categoryList.add(c.getCategoryName());
        }
        return categoryList;
    }
}
