package com.TTT.TTT.Likes.Repository;

import com.TTT.TTT.Likes.domain.Likes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikesRepository extends JpaRepository<Likes,Long> {
    Optional<Likes> findByPost_IdAndUser_Id(Long postId,Long userId);
    Optional<Likes> findByProject_IdAndUser_Id(Long postId,Long userId);
}
