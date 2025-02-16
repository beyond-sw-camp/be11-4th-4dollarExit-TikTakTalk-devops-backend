package com.TTT.TTT.Post.repository;

import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.Post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>{
//전체게시판 글 조회
    Page<Post> findAllByDelYN(DelYN delYN,Pageable pageable);
//검색용. delYN은 기본적으로 삭제되지 않은 것 중에 검색되게 specification으로 구현
    Page<Post> findAll(Specification<Post> specification, Pageable pageable);

    Optional<Post> findByIdAndDelYN(Long id,DelYN delYN);

//자유게시판 글 조회
    Page<Post> findAllByCategory_IdAndDelYN(Long id,DelYN delYN,Pageable pageable);
}
