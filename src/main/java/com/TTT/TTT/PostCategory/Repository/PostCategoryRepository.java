package com.TTT.TTT.PostCategory.Repository;

import com.TTT.TTT.Post.domain.Post;
import com.TTT.TTT.PostCategory.domain.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostCategoryRepository extends JpaRepository<PostCategory,Long> {

    Optional<PostCategory> findByCategoryName(String name);
}
