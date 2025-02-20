package com.TTT.TTT.Post.controller;

import com.TTT.TTT.Common.dtos.CommonDto;
import com.TTT.TTT.Post.dtos.*;
import com.TTT.TTT.Post.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import retrofit2.http.Path;

import java.util.List;

@RestController
@RequestMapping("/ttt/post")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

//  1.게시글 생성(폼데이터 방식)
    @PostMapping("/create")
    public ResponseEntity<?> createPost(@Valid PostCreateDto dto, List<MultipartFile> attachments) {
        postService.save(dto, attachments);
        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(),"post create success","OK"),HttpStatus.CREATED);
    }

//  2.게시글 조회()
    @GetMapping("/findAll")
    public ResponseEntity<?> findAll(@PageableDefault(size = 20, sort = "createdTime" , direction = Sort.Direction.DESC) Pageable pageable){
        Page<PostAllListDto> postPage = postService.findAll(pageable);
                return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "List is uploaded successfully",postPage),HttpStatus.OK);
    }

//    3.게시글 검색
    @GetMapping("/find")
    public ResponseEntity<?> getAllPost(PostSearchDto postSearchDto,
                                        Pageable pageable) {

      Page<PostListDto> specificPostList =postService.findAll(postSearchDto,pageable);
      return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"postList is uploaded successfully",specificPostList),HttpStatus.OK);
    }

//   4.게시글 상세보기
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
       PostDetailDto postDetailDto = postService.findById(id);
       return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "the post is uploaded successfully",postDetailDto),HttpStatus.OK);

    }

//   5.게시글 수정
    @PatchMapping("/update/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id,@Valid PostUpdateDto postUpdateDto, List<MultipartFile> attachments){
        postService.updatePost(id,postUpdateDto,attachments);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(),"update is success","success"),HttpStatus.OK);
    }


//    6.게시글 삭제
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        postService.deleteById(id);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "post delete success","OK"), HttpStatus.OK);
    }

//    7.특정게시판 조회(1번-자유게시판, 2번-정보게시판)
    @GetMapping("/category/{id}")
    public ResponseEntity<?> selectedBoard(@PathVariable Long id,@PageableDefault(size = 20,sort = "createdTime",direction = Sort.Direction.DESC) Pageable pageable){
       Page<PostListDto> selectedList = postService.selectedBoard(id,pageable);
       return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "selected board is uploaded successfully",selectedList),HttpStatus.OK);
    }

//    +이미지 업로드(드래그 앤 드롭 했을때 이미지 바로 저장)
//    @PostMapping("/drag-image")
//    public ResponseEntity<?> dragImages(@RequestParam MultipartFile attachments){
//
//    }



}
