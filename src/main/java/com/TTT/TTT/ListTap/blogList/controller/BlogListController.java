package com.TTT.TTT.ListTap.blogList.controller;

import com.TTT.TTT.Common.dtos.CommonDto;
import com.TTT.TTT.ListTap.blogList.dtos.BlogLinkResponseDto;
import com.TTT.TTT.ListTap.blogList.dtos.BlogLinkSearchDto;
import com.TTT.TTT.ListTap.blogList.service.BlogListService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("ttt/blog")
public class BlogListController {

    private final BlogListService blogListService;

    public BlogListController(BlogListService blogListService) {
        this.blogListService = blogListService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> getBlogLink(@PageableDefault(size = 20) Pageable pageable){
        Page<BlogLinkResponseDto> list =   blogListService.getAllBlogLinks(pageable);
            return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "list is uploaded well",list),HttpStatus.OK);
    }
    @GetMapping("/find")
    public ResponseEntity<?> getAllList(BlogLinkSearchDto blogLinkSearchDto,@PageableDefault(size = 20) Pageable pageable){
        Page<BlogLinkResponseDto> specificBlogLinkList = blogListService.getAllBlogLinks(blogLinkSearchDto,pageable);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "blogLink is uploaded well",specificBlogLinkList),HttpStatus.OK);
    }
}
