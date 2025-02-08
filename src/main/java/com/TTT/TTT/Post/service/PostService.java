package com.TTT.TTT.Post.service;

import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.Post.domain.Post;
import com.TTT.TTT.Post.dtos.PostDetailRes;
import com.TTT.TTT.Post.dtos.PostListRes;
import com.TTT.TTT.Post.dtos.PostSaveReq;
import com.TTT.TTT.Post.dtos.PostUpdateReq;
import com.TTT.TTT.Post.repository.PostRepository;
import com.TTT.TTT.User.UserRepository.UserRepository;
import com.TTT.TTT.User.domain.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    // 모든 게시글 조회
    public List<PostListRes> findAll() {
        return postRepository.findAllByDelYn(DelYN.N).stream()
                .map(post -> new PostListRes(
                        post.getId(),
                        post.getTitle(),
                        post.getContents().substring(0, Math.min(post.getContents().length(), 50)), // 내용 요약
                        post.getCreatedTime().toString()))
                .toList();
    }

    // 2. 특정 게시글 조회
    public PostDetailRes findById(Long id) {
        Post post = postRepository.findByIdAndDelYn(id, DelYN.N)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. ID: " + id));
        return new PostDetailRes(
                post.getId(),
                post.getTitle(),
                post.getContents(),
                post.getCreatedTime().toString());
    }

    // 3. 게시글 생성
    public void save(PostSaveReq dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();
        User user = userRepository.findByLoginIdAndDelYN(loginId,DelYN.N).orElseThrow(() -> new EntityNotFoundException("user is not found"));

        Post post = Post.builder()
                .title(dto.getTitle())
                .contents(dto.getContent())
                .user(user)
                .delYn(DelYN.N)
                .build();
        postRepository.save(post);
    }

    // 4. 게시글 수정
    public void update(Long id, PostUpdateReq dto) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            throw new IllegalArgumentException("사용자 인증 필요");
        }

        UserDetails userDetails = (UserDetails) principal;
        String loginId = userDetails.getUsername();

        Post post = postRepository.findByIdAndDelYn(id,DelYN.N).orElseThrow(() -> new EntityNotFoundException("post is not found or deleted"));

        post.update(dto.getTitle(),dto.getContent());
        postRepository.save(post);
    }


    // 5. 게시글 삭제
    public void delete(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();
        User user = userRepository.findByLoginIdAndDelYN(loginId, DelYN.N).orElseThrow(()-> new EntityNotFoundException("user is not found"));
        Post post = postRepository.findByIdAndDelYn(id, DelYN.N).orElseThrow(()-> new EntityNotFoundException("post is not found"));
        post.deletePost();
    }

}
