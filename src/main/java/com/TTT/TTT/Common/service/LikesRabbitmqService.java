package com.TTT.TTT.Common.service;

import com.TTT.TTT.Common.configs.RabbitmqConfig;
import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.Common.dtos.BackupForLikesDto;
import com.TTT.TTT.Likes.Repository.LikesRepository;
import com.TTT.TTT.Likes.domain.Likes;
import com.TTT.TTT.ListTap.projectList.domain.Project;
import com.TTT.TTT.ListTap.projectList.repository.ProjectRepository;
import com.TTT.TTT.Post.domain.Post;
import com.TTT.TTT.Post.repository.PostRepository;
import com.TTT.TTT.User.UserRepository.UserRepository;
import com.TTT.TTT.User.domain.User;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Member;

@Service
@Transactional
public class LikesRabbitmqService {
    private final RabbitTemplate template;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikesRepository likesRepository;
    private final ProjectRepository projectRepository;

    public LikesRabbitmqService(RabbitTemplate template, PostRepository postRepository, UserRepository userRepository, LikesRepository likesRepository, ProjectRepository projectRepository) {
        this.template = template;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likesRepository = likesRepository;
        this.projectRepository = projectRepository;
    }

    public void publishForAdding(BackupForLikesDto dto){
        template.convertAndSend(RabbitmqConfig.BACKUP_QUEUE_AL,dto);
    }

    @RabbitListener(queues = RabbitmqConfig.BACKUP_QUEUE_AL)
    public void subscribeForAdding(Message message) throws JsonProcessingException {
        String messgeBody = new String(message.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        BackupForLikesDto dto = objectMapper.readValue(messgeBody, BackupForLikesDto.class);
        Post post = postRepository.findById(dto.getPostId()).orElseThrow(()-> new EntityNotFoundException("게시글이 존재하지 않습니다"));
        User user = userRepository.findByLoginIdAndDelYN(dto.getUserId(), DelYN.N).orElseThrow(()->new EntityNotFoundException("유저가 존재하지않습니다"));

        Likes likes = Likes.builder()
                .user(user)
                .post(post)
                .build();
        likesRepository.save(likes);
    }

    public void publishForMinus(BackupForLikesDto dto){
        template.convertAndSend(RabbitmqConfig.BACKUP_QUEUE_ML,dto);
    }

    @RabbitListener(queues = RabbitmqConfig.BACKUP_QUEUE_ML)
    public void subscribeForMinus(Message message) throws JsonProcessingException {
        String messgeBody = new String(message.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        BackupForLikesDto dto = objectMapper.readValue(messgeBody, BackupForLikesDto.class);
        Post post = postRepository.findById(dto.getPostId()).orElseThrow(()-> new EntityNotFoundException("게시글이 존재하지 않습니다"));
        User user = userRepository.findByLoginIdAndDelYN(dto.getUserId(), DelYN.N).orElseThrow(()->new EntityNotFoundException("유저가 존재하지않습니다"));
        Likes cancelledLike = likesRepository.findByPost_IdAndUser_Id(post.getId(),user.getId()).orElseThrow(()->new EntityNotFoundException("해당 좋아요가 없습니다."));
        likesRepository.delete(cancelledLike);
    }

//    여기서부터 프로젝트 좋아요 관련 래빗엠큐


//    public void publishForAddingForProject(BackupForLikesDto dto){
//        template.convertAndSend(RabbitmqConfig.BACKUP_QUEUE_ALFORP,dto);
//    }
//
//    @RabbitListener(queues = RabbitmqConfig.BACKUP_QUEUE_ALFORP)
//    public void subscribeForAddingForProject(Message message) throws JsonProcessingException {
//        String messgeBody = new String(message.getBody());
//        ObjectMapper objectMapper = new ObjectMapper();
//        BackupForLikesDto dto = objectMapper.readValue(messgeBody, BackupForLikesDto.class);
//        Project project = projectRepository.findById(dto.getPostId()).orElseThrow(()-> new EntityNotFoundException("프로젝트가 존재하지 않습니다"));
//        User user = userRepository.findByLoginIdAndDelYN(dto.getUserId(), DelYN.N).orElseThrow(()->new EntityNotFoundException("유저가 존재하지않습니다"));
//
//        Likes likes = Likes.builder()
//                .user(user)
//                .project(project)
//                .build();
//        likesRepository.save(likes);
//    }
//
//    public void publishForMinusForProject(BackupForLikesDto dto){
//        template.convertAndSend(RabbitmqConfig.BACKUP_QUEUE_MLFORP,dto);
//    }
//
//    @RabbitListener(queues = RabbitmqConfig.BACKUP_QUEUE_MLFORP)
//    public void subscribeForMinusForProject(Message message) throws JsonProcessingException {
//        String messgeBody = new String(message.getBody());
//        ObjectMapper objectMapper = new ObjectMapper();
//        BackupForLikesDto dto = objectMapper.readValue(messgeBody, BackupForLikesDto.class);
//        Project project = projectRepository.findById(dto.getPostId()).orElseThrow(()-> new EntityNotFoundException("프로젝트가 존재하지 않습니다"));
//        User user = userRepository.findByLoginIdAndDelYN(dto.getUserId(), DelYN.N).orElseThrow(()->new EntityNotFoundException("유저가 존재하지않습니다"));
//        Likes cancelledLike = likesRepository.findByProject_IdAndUser_Id(project.getId(),user.getId()).orElseThrow(()->new EntityNotFoundException("해당 좋아요가 없습니다."));
//        likesRepository.delete(cancelledLike);
//    }


}

