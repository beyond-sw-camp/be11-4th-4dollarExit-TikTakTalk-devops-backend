package com.TTT.TTT.Comment.Service;

import com.TTT.TTT.Comment.Dtos.CommentCreateDto;
import com.TTT.TTT.Comment.Dtos.CommentUpdateDto;
import com.TTT.TTT.Comment.Repository.CommentRepository;
import com.TTT.TTT.Comment.domain.Comment;
import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.ListTap.projectList.domain.Project;
import com.TTT.TTT.ListTap.projectList.repository.ProjectRepository;
import com.TTT.TTT.Post.domain.Post;
import com.TTT.TTT.Post.repository.PostRepository;
import com.TTT.TTT.User.UserRepository.UserRepository;
import com.TTT.TTT.User.domain.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ProjectRepository projectRepository;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository, PostRepository postRepository, ProjectRepository projectRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.projectRepository = projectRepository;
    }
//        1.댓글달기
        public void save(CommentCreateDto commentCreateDto){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = userRepository.findByLoginIdAndDelYN(authentication.getName(), DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 아이디입니다"));
            Post post = postRepository.findById(commentCreateDto.getPostId()).orElseThrow(()->new EntityNotFoundException("없는 게시글입니다"));
            //부모댓글값이 없으면 원댓글로 저장
            if(commentCreateDto.getParentId()==null){
                Comment comment = Comment.builder()
                        .contents(commentCreateDto.getContents())
                        .user(user)
                        .post(post)
                        .build();
                commentRepository.save(comment);
            //부모댓글값이 있으면 대댓글로 저장
            } else{
                Comment parentComment = commentRepository.findById(commentCreateDto.getParentId()).orElseThrow(()->new EntityNotFoundException("원댓글이 없습니다"));


                Comment comment = Comment.builder()
                        .contents(commentCreateDto.getContents())
                        .user(user)
                        .post(post)
                        .parent(parentComment)
                        .build();
                commentRepository.save(comment);
            }

            User postAuthor = post.getUser();
            if(!user.equals(postAuthor)){ // 댓글 달린 게시글 작성자한테 10점 추가(단, 본인이 쓴 댓글 제외하고)
                user.rankingPointUpdate(+10); //댓글 생성시 10점 추가
                postAuthor.rankingPointUpdate(+10);
                userRepository.save(postAuthor); //다시 저장하여 점수 업데이트
            }
    }

//   2.프로젝트에 댓글 달기
    public void saveForP(CommentCreateDto commentCreateDto){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByLoginIdAndDelYN(authentication.getName(), DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 아이디입니다"));
        Project project = projectRepository.findById(commentCreateDto.getProjectId()).orElseThrow(()->new EntityNotFoundException("없는 프로젝트입니다"));
        //부모댓글값이 없으면 원댓글로 저장
        if(commentCreateDto.getParentId()==null){
            Comment comment = Comment.builder()
                    .contents(commentCreateDto.getContents())
                    .user(user)
                    .project(project)
                    .build();
            commentRepository.save(comment);
            //부모댓글값이 있으면 대댓글로 저장
        } else{
            Comment parentComment = commentRepository.findById(commentCreateDto.getParentId()).orElseThrow(()->new EntityNotFoundException("원댓글이 없습니다"));


            Comment comment = Comment.builder()
                    .contents(commentCreateDto.getContents())
                    .user(user)
                    .project(project)
                    .parent(parentComment)
                    .build();
            commentRepository.save(comment);
        }

    }
//    2.댓글 수정
    public void update(CommentUpdateDto commentUpdateDto,Long id){
            String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
            User loginUser = userRepository.findByLoginIdAndDelYN(loginId,DelYN.N).orElseThrow(()->new EntityNotFoundException("존재하지 않는 유저입니다"));
            Comment comment = commentRepository.findByIdAndDelYN(id,DelYN.N).orElseThrow(()->new EntityNotFoundException("존재 하지않는 댓글입니다"));
            if(!loginUser.equals(comment.getUser())){
                throw new AccessDeniedException("해당 댓글의 작성자만이 댓글을 수정할 수 있습니다");
            }
            comment.update(commentUpdateDto.getContents());
        }


//    3.댓글 삭제
    public void delete(Long id){
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        User loginUser = userRepository.findByLoginIdAndDelYN(loginId,DelYN.N).orElseThrow(()->new EntityNotFoundException("존재하지 않는 유저입니다"));
        Comment comment = commentRepository.findById(id).orElseThrow(()->new EntityNotFoundException("존재 하지않는 댓글입니다"));
        if(!loginUser.equals(comment.getUser())){
            throw new AccessDeniedException("해당 댓글의 작성자만이 댓글을 수정할 수 있습니다");
        }
        comment.delete();
        commentRepository.save(comment);
        loginUser.rankingPointUpdate(-10);//댓글 삭제시 10점 회수
    }
    }

