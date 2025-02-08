package com.TTT.TTT.Post.repository;

import com.TTT.TTT.Common.DelYN;
import com.TTT.TTT.Post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByDelYn(DelYN delYn);

    Optional<Post> findByIdAndDelYn(Long id, DelYN delYn);
}
