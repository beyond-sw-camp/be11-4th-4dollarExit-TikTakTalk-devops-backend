package com.TTT.TTT.Post.service;

import com.TTT.TTT.Post.domain.Post;
import com.TTT.TTT.Post.dtos.PostDetailRes;
import com.TTT.TTT.Post.dtos.PostListRes;
import com.TTT.TTT.Post.dtos.PostSaveReq;
import com.TTT.TTT.Post.dtos.PostUpdateReq;
import com.TTT.TTT.Post.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // 모든 게시글 조회
    public List<PostListRes> findAll() {
        return postRepository.findAll().stream()
                .map(post -> new PostListRes(
                        post.getId(),
                        post.getTitle(),
                        post.getContent().substring(0, Math.min(post.getContent().length(), 50)), // 내용 요약
                        post.getCreatedTime().toString()))
                .toList();
    }

    // 2. 특정 게시글 조회
    public PostDetailRes findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. ID: " + id));
        return new PostDetailRes(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedTime().toString());
    }

    // 3. 게시글 생성
    public void save(PostSaveReq dto, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다. ID: " + userId));

        Post post = Post.builder()
                .title(dto.getTitle())
                .contents(dto.getContent())
                .build();
        postRepository.save(post);
    }

    // 4. 게시글 수정
    public void update(Long id, PostUpdateReq dto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다." ));
        post.update(dto.getTitle(), dto.getContent());
        postRepository.save(post);
    }

    // 5. 게시글 삭제
    public void delete(Long id) {
        postRepository.deleteById(id);
    }
}