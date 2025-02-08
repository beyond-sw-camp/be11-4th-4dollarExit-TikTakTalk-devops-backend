package com.TTT.TTT.Comment.Repository;


import com.TTT.TTT.Comment.domain.Comment;
import com.TTT.TTT.Common.domain.DelYN;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    Optional<Comment> findByIdAndDelYN(Long id, DelYN delYN);
}
