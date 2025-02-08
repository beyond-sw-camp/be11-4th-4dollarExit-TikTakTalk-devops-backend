package com.TTT.TTT.Post.controller;

import com.TTT.TTT.Common.dtos.CommonDto;
import com.TTT.TTT.Post.dtos.PostDetailRes;
import com.TTT.TTT.Post.dtos.PostListRes;
import com.TTT.TTT.Post.dtos.PostSaveReq;
import com.TTT.TTT.Post.dtos.PostUpdateReq;
import com.TTT.TTT.Post.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ttt/post")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 전체조회 (카테고리 추가 필요)
    @GetMapping("/findAll")
    public List<PostListRes> getAllPost() {
        return postService.findAll();
    }

    //    게시글 상세 조회
    @GetMapping("/detail/{id}")
    public PostDetailRes getPostById(@PathVariable Long id) {
        return postService.findById(id); // ID로 게시글 조회 (수정가능)
    }

    // 게시글 수정
    @PatchMapping("/update/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody PostUpdateReq dto) {
        postService.update(id, dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"post update success","ok"), HttpStatus.OK );
    }

    // 게시글 삭제 (DelYn으로 수정 필요)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        postService.delete(id);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "post delete success","OK"), HttpStatus.OK);
    }

    // 게시글 생성
    @PostMapping("/create")
    public ResponseEntity<?> createPost(@RequestBody PostSaveReq dto) {
        postService.save(dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(),"post create success","OK"),HttpStatus.CREATED);
    }

//    검색 추가 필요
//    게시글 생성
//

}
