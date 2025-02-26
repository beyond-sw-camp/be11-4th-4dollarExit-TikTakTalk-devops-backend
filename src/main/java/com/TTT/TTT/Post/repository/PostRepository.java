package com.TTT.TTT.Post.repository;

import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.Post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>{
//전체게시판 글 조회
    Page<Post> findAllByDelYN(DelYN delYN,Pageable pageable);
//전체게시글
//    value
    @Query(value = "SELECT p FROM Post p JOIN FETCH p.user WHERE p.delYN = :delYN",
            countQuery = "SELECT COUNT(p) FROM Post p WHERE p.delYN = :delYN")
    Page<Post> findAllWithUser(@Param("delYN") DelYN delYN, Pageable pageable);

//검색용. delYN은 기본적으로 삭제되지 않은 것 중에 검색되게 specification으로 구현
    Page<Post> findAll(Specification<Post> specification, Pageable pageable);

    Optional<Post> findByIdAndDelYN(Long id,DelYN delYN);

//자유게시판 글 조회
    Page<Post> findAllByCategory_IdAndDelYN(Long id,DelYN delYN,Pageable pageable);

//
//    @Query("SELECT p FROM Post p  JOIN FETCH p.user WHERE p.category = :id AND p.delYN = :delYN")
//    Page<Post> findAllwithUser(@Param("category")Long id, @Param("delYN")DelYN delYN, Pageable pageable);

//전체게시글 중에 인기게시글 조회
    List<Post> findTop10ByOrderByLikesCountDesc();

    @Modifying // 업데이트,딜리트 쿼리를 실행하기 위한 JPA어노테이션
    @Query("Update Post p SET p.viewCount = :newview WHERE p.id = :postId") //매개변수로 준 게시물 id에 해당하는 게시물의 조회수칼럼을 매개변수로 준 newview로 업데이트한다는 말
    @Transactional
//    여기서 Param은 쿼리 내의 :postId와 :increment와 매핑되기 위해 사용한다.
    public void increaseViewCountByValue(@Param("postId")Long postId, @Param("newview")Integer newview);

//    오늘 날짜의 좋아요가 가장 많은 12개 게시글을 가져옴
    List<Post> findTop12ByCreatedTimeBetweenOrderByLikesCountDescCreatedTimeAsc(LocalDateTime startOfDay, LocalDateTime endOfDay);


}
