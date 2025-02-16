package com.TTT.TTT.ListTap.blogList.service;

import com.TTT.TTT.ListTap.blogList.dtos.BlogLinkResponseDto;
import com.TTT.TTT.User.UserRepository.UserRepository;
import com.TTT.TTT.User.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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



}
