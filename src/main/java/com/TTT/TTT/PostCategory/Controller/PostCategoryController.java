package com.TTT.TTT.PostCategory.Controller;

import com.TTT.TTT.Common.dtos.CommonDto;
import com.TTT.TTT.PostCategory.Dtos.PostCategoryListDto;
import com.TTT.TTT.PostCategory.Service.PostCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ttt/category")
public class PostCategoryController {

    private final PostCategoryService postCategoryService;


    public PostCategoryController(PostCategoryService postCategoryService) {
        this.postCategoryService = postCategoryService;
    }

//  1.카테고리 리스트 조회
    @GetMapping("/all")
    public ResponseEntity<?> categoryList(){
      List<String> list=postCategoryService.findAll();
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "list is uploaded successfully",list),HttpStatus.OK);
    }

}
