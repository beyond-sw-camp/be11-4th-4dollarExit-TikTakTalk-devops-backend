package com.TTT.TTT.ListTap.blogList.service;

import com.TTT.TTT.ListTap.blogList.dtos.BlogLinkResponseDto;
import com.TTT.TTT.ListTap.blogList.dtos.BlogLinkSearchDto;
import com.TTT.TTT.User.UserRepository.UserRepository;
import com.TTT.TTT.User.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class BlogListService {

        private final UserRepository userRepository;

    public BlogListService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


//    1.조회
    public Page<BlogLinkResponseDto> getAllBlogLinks(Pageable pageable){
               Page<User> userList = userRepository.findAll(pageable);
                Page<BlogLinkResponseDto> userList2 = userList.map(u->u.toBlogDto());
                return userList2;
    }
//    2. 검색
public Page<BlogLinkResponseDto> getAllBlogLinks(BlogLinkSearchDto blogLinkSearchDto, Pageable pageable) {
    Specification<User> specification = (root, query, criteriaBuilder) -> {
        List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

        // 🔍 이름 검색 (LIKE)
        if (blogLinkSearchDto.getName() != null && !blogLinkSearchDto.getName().isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("name"), "%" + blogLinkSearchDto.getName() + "%"));
        }

        // 🔍 기수(batch) 검색 (정확한 일치 검색)
        if (blogLinkSearchDto.getBatch() != null) {
            predicates.add(criteriaBuilder.equal(root.get("batch"), blogLinkSearchDto.getBatch()));
        }

        return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
    };

    // ✅ UserRepository에서 검색 실행
    Page<User> originalUserList = userRepository.findAll(specification, pageable);

    // ✅ 결과를 DTO로 변환하여 반환
    return originalUserList.map(User::toBlogDto);
}



}
