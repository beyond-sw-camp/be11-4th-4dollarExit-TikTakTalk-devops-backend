package com.TTT.TTT.Likes.Service;
import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.Common.dtos.BackupForLikesDto;
import com.TTT.TTT.Common.service.LikesRabbitmqService;
import com.TTT.TTT.Likes.Repository.LikesRepository;
import com.TTT.TTT.Likes.domain.Likes;
import com.TTT.TTT.ListTap.projectList.domain.Project;
import com.TTT.TTT.ListTap.projectList.repository.ProjectRepository;
import com.TTT.TTT.Post.domain.Post;
import com.TTT.TTT.Post.repository.PostRepository;
import com.TTT.TTT.User.UserRepository.UserRepository;
import com.TTT.TTT.User.domain.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
@Transactional
public class LikesService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final LikesRabbitmqService likesRabbitmqService;

@Qualifier("likes")
    private final RedisTemplate<String,String> redisTemplate;
@Qualifier("project")
    private final RedisTemplate<String,String> redisTemplateForProject;


    public LikesService(PostRepository postRepository, UserRepository userRepository, ProjectRepository projectRepository
            , LikesRabbitmqService likesRabbitmqService, @Qualifier("likes") RedisTemplate<String, String> redisTemplate, @Qualifier("project") RedisTemplate<String, String> redisTemplateForProject) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.likesRabbitmqService = likesRabbitmqService;

        this.redisTemplate = redisTemplate;
        this.redisTemplateForProject = redisTemplateForProject;
    }
//게시물 좋아요
    public Map<String, Object> toggleLike(Long postId){
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        User loginUser = userRepository.findByLoginIdAndDelYN(loginId, DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 사용자입니다"));
        Post post = postRepository.findById(postId).orElseThrow(()-> new EntityNotFoundException("없는 게시글입니다"));
        User AuthorOfPost = userRepository.findByLoginIdAndDelYN(post.getUser().getLoginId(),DelYN.N).orElseThrow(()->new EntityNotFoundException("게시글 작성자가 존재하지 않습니다"));

        BackupForLikesDto dto = BackupForLikesDto.builder().PostId(postId).UserId(loginId).build();//레디스 mq에 들어갈 백업용dto

        String likeUserKey = "post-" + postId +"-likeUsers"; //레디스에 해당 포스트에 좋아요 누른 목록사람에 대해 저장될 키값(밸류는 셋 자료구조를 이용한 좋아요 누른 사람 목록) ex.post-1-likeusers : 5 이렇게 저장될 수 있도록
        String likeCountKey = "post-" + postId + "-likeCount"; //레디스에 해당 포스트의 좋아요 개수와 관련한 키값(밸류는 좋아요 개수)
        String likeMyListKey = "user-"+ loginId + "-myLikeList"; //레디스에 해당 유저가 좋아요한 포스트id를 저장.그에 대한 키값

        if(redisTemplate.opsForSet().isMember(likeUserKey,loginUser.getLoginId())){ //해당 포스트 좋아요 누른 유저 목록에 지금 유저 아이디가 있다면, 즉 이미 좋아요를 누른사람이라면
            redisTemplate.opsForSet().remove(likeUserKey,loginUser.getLoginId()); //해당 목록에서 현재 유저를 제거
            redisTemplate.opsForValue().decrement(likeCountKey);//그리고 좋아요 개수에서 1을 뺀다
            redisTemplate.opsForSet().remove(likeMyListKey,String.valueOf(post.getId()));//내가 좋아료 한 글 목록에 해당 포스트 아이디를 삭제한다.

            likesRabbitmqService.publishForMinus(dto);//rbd에서 좋아요 삭제
        } else{
            redisTemplate.opsForSet().add(likeUserKey,loginUser.getLoginId());//해당 포스트 좋아요 누른 유저목록에 현재 유저를 추가
            redisTemplate.opsForValue().increment(likeCountKey);//그리고 좋아요 개수를 1증가시킨다
            redisTemplate.opsForSet().add(likeMyListKey,String.valueOf(post.getId()));//내가 좋아요 한 글 목록에 해당 포스트 아이디를 저장시킨다.

            likesRabbitmqService.publishForAdding(dto); //rdb에서 좋아요 추가
            if(loginUser!=AuthorOfPost) {
                AuthorOfPost.rankingPointUpdate(10);// 좋아요 받은 글작성자 점수 +10(자기 글에 좋아요 눌러도 점수 안오름)
            }
        }
        boolean liked = redisTemplate.opsForSet().isMember(likeUserKey,loginUser.getLoginId()); // 해당 유저가 좋아요 눌렀는지 아닌 지 불린값
        Map<String,Object> likeStatus = new HashMap<>();
        likeStatus.put("likesCount",Long.parseLong(redisTemplate.opsForValue().get(likeCountKey)));
        likeStatus.put("liked",liked);

        return likeStatus;
    }

//프로젝트 좋아요
    public Map<String,Object> toggleLikeForProject(Long projectId){
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        User loginUser = userRepository.findByLoginIdAndDelYN(loginId, DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 사용자입니다"));
        Project project = projectRepository.findById(projectId).orElseThrow(()-> new EntityNotFoundException("없는 게시글입니다"));
//        User AuthorOfPost = userRepository.findByLoginIdAndDelYN(post.getUser().getLoginId(),DelYN.N).orElseThrow(()->new EntityNotFoundException("게시글 작성자가 존재하지 않습니다"));

        BackupForLikesDto dto = BackupForLikesDto.builder().projectId(projectId).UserId(loginId).build();//레디스 mq에 들어갈 백업용dto

        String likeUserKey = "project-" + projectId +"-likeUsers"; //레디스에 해당 프로젝트에 좋아요 누른 목록사람에 대해 저장될 키값(밸류는 셋 자료구조를 이용한 좋아요 누른 사람 목록) ex.project-1-likeusers : 5 이렇게 저장될 수 있도록
        String likeCountKey = "project-" + projectId + "-likeCount"; //레디스에 해당 프로젝트의 좋아요 개수와 관련한 키값(밸류는 좋아요 개수)
        String likeMyListKey = "user-"+ loginId + "-myLikeProject"; //레디스에 해당 유저가 좋아요한 포스트id를 저장.그에 대한 키값

        if(redisTemplateForProject.opsForSet().isMember(likeUserKey,loginUser.getLoginId())){ //해당 포스트 좋아요 누른 유저 목록에 지금 유저 아이디가 있다면, 즉 이미 좋아요를 누른사람이라면
            redisTemplateForProject.opsForSet().remove(likeUserKey,loginUser.getLoginId()); //해당 목록에서 현재 유저를 제거
            redisTemplateForProject.opsForValue().decrement(likeCountKey);//그리고 좋아요 개수에서 1을 뺀다
            redisTemplateForProject.opsForSet().remove(likeMyListKey,String.valueOf(project.getId()));//내가 좋아료 한 글 목록에 해당 프로젝트 아이디를 삭제한다.

//            likesRabbitmqService.publishForMinusForProject(dto);//rbd에서 좋아요 삭제
        } else{
            redisTemplateForProject.opsForSet().add(likeUserKey,loginUser.getLoginId());//해당 프로젝트 좋아요 누른 유저목록에 현재 유저를 추가
            redisTemplateForProject.opsForValue().increment(likeCountKey);//그리고 좋아요 개수를 1증가시킨다
            redisTemplateForProject.opsForSet().add(likeMyListKey,String.valueOf(project.getId()));//내가 좋아요 한 프로젝트 목록에 해당 프로젝트 아이디를 저장시킨다.

//            likesRabbitmqService.publishForAddingForProject(dto); //rdb에서 좋아요 추가

        }
        boolean liked = redisTemplateForProject.opsForSet().isMember(likeUserKey,loginUser.getLoginId()); // 해당 유저가 좋아요 눌렀는지 아닌 지 불린값
        Map<String,Object> likeStatus = new HashMap<>();
        likeStatus.put("likesCount",Long.parseLong(redisTemplateForProject.opsForValue().get(likeCountKey)));
        likeStatus.put("liked",liked);

        return likeStatus;





    }
}

