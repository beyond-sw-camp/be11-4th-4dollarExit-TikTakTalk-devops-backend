package com.TTT.TTT.Post.contorller;

import com.TTT.TTT.Post.dtos.PostDetailRes;
import com.TTT.TTT.Post.dtos.PostListRes;
import com.TTT.TTT.Post.dtos.PostUpdateReq;
import com.TTT.TTT.Post.service.PostService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 전체조회 (카테고리 추가 필요)
    @GetMapping
    public List<PostListRes> getAllPost() {
        return postService.findAll();
    }

    //    게시글 조회
    @GetMapping("/{id}")
    public PostDetailRes getPostById(@PathVariable Long id) {
        return postService.findById(id); // ID로 게시글 조회 (수정가능)
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public void updatePost(@PathVariable Long id, @RequestBody PostUpdateReq dto) {
        postService.update(id, dto);
    }

    // 게시글 삭제 (DelYn으로 수정 필요)
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        postService.delete(id);
    }

    // 게시글 생성
    @PostMapping
    public void createPost(@RequestBody PostUpdateReq dto) {
        postService.create(dto);
    }

//    검색 추가 필요
//    게시글 생성
//

}
