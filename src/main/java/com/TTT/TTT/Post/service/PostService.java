package com.TTT.TTT.Post.service;

import com.TTT.TTT.Attachment.Domain.Attachment;
import com.TTT.TTT.Attachment.Repository.AttachmentRepository;
import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.Post.domain.Post;
import com.TTT.TTT.Post.dtos.*;
import com.TTT.TTT.Post.repository.PostRepository;
import com.TTT.TTT.PostCategory.Repository.PostCategoryRepository;
import com.TTT.TTT.PostCategory.domain.PostCategory;
import com.TTT.TTT.User.UserRepository.UserRepository;
import com.TTT.TTT.User.domain.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final S3Client s3Client;

    public PostService(PostRepository postRepository, UserRepository userRepository, AttachmentRepository attachmentRepository,PostCategoryRepository postCategoryRepository,S3Client s3Client) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.attachmentRepository = attachmentRepository;
        this.postCategoryRepository = postCategoryRepository;
        this.s3Client = s3Client;
    }

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

//  1.게시글 생성
    public void save(Long id,PostCreateDto dto, List<MultipartFile> attachments) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();
        User user = userRepository.findByLoginIdAndDelYN(loginId, DelYN.N).orElseThrow(() -> new EntityNotFoundException("없는 아이디입니다"));
        PostCategory postCategory = postCategoryRepository.findById(id).orElseThrow(()->new EntityNotFoundException("없는 게시판입니다"));
       //첨부파일 속성없이 일단 포스트 레포지토리에 저장
        Post post = Post.builder()
                .title(dto.getTitle())
                .contents(dto.getContents())
                .user(user)
                .delYN(DelYN.N)
                .category(postCategory)
                .build();
        postRepository.save(post);
        user.rankingPointUpdate(20); // 게시글 작성시 랭킹점수 20점 상승
        //다시 게시글 속성에 첨부파일 추가해야 하니 먼저 리스트 만들어 놓음
        //그런데 첨부파일 없는 게시글은 nullPointerException이 놓으니까 if절
        if(attachments !=null) {
            List<Attachment> attachmentsOfPost = post.getAttachmentList();

            for (MultipartFile f : attachments) {
                try {
                    //로컬에 저장
                    int z = 1;//한 게시글에 여러 글올라오니까 이름중복 막기 위해서 둔 변수
                    byte[] bytes = f.getBytes();
                    String fileName = post.getId() + "_" + z + f.getOriginalFilename();
                    z++;
                    Path path = Paths.get("C:/Users/Playdata/Desktop/tmp/", fileName);
                    Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    //aws에 저장
                    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(fileName)
                            .build();
                    s3Client.putObject(putObjectRequest, RequestBody.fromFile(path));
                    String s3Url = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(fileName)).toExternalForm();
                    //attachment 엔티티 생성하여 포스트의 첨부파일리스트 속성에 저장
                    Attachment attachment = Attachment.builder()
                            .fileName(fileName)
                            .urlAdress(s3Url)
                            .post(post)
                            .build();
                    attachmentsOfPost.add(attachment);
                    attachmentRepository.save(attachment);
                } catch (IOException e) {
                    throw new RuntimeException("이미지 저장 실패");

                }
            }
        }

    }

//    2.게시글 조회
 public Page<PostAllListDto> findAll(Pageable pageable){
     Page<Post> originalPostList =  postRepository.findAllByDelYN(DelYN.N,pageable);
     return originalPostList.map(p->p.toAllListDto());
    }


//   3.게시글 검색
    public Page<PostListDto> findAll(PostSearchDto postSearchDto, Pageable pageable) {
        Specification<Post> specification = new Specification<Post>() {
            @Override
            public Predicate toPredicate(Root<Post> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (postSearchDto.getTitle() != null) {
                    predicates.add(criteriaBuilder.like(root.get("title"), "%" + postSearchDto.getTitle() + "%"));
                }
                if (postSearchDto.getContents() != null) {
                    predicates.add(criteriaBuilder.like(root.get("contents"), "%" + postSearchDto.getContents() + "%"));
                }
                predicates.add(criteriaBuilder.equal((root.get("delYN")),DelYN.N));
                Predicate[] predicateArr = new Predicate[predicates.size()];
                for (int i = 0; i < predicates.size(); i++) {
                    predicateArr[i] = predicates.get(i);
                }
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };
        Page<Post> originalPostList = postRepository.findAll(specification, pageable);
        return originalPostList.map(p->p.toListDto());
    }

//    4.게시글 상세보기
    public PostDetailDto findById(Long id){
        Post post = postRepository.findByIdAndDelYN(id,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 게시글입니다"));
        return post.toDetailDto();

    }


//    5.게시글 수정
    public void updatePost(Long id, PostUpdateDto postUpdateDto, List<MultipartFile> attachments){
        String idOfAuthor = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByLoginIdAndDelYN(idOfAuthor,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 사용자입니다"));
        Post originalPost = postRepository.findById(id).orElseThrow(()->new EntityNotFoundException("없는 게시글입니다"));
        //수정하려는 로그인한 유저가 이 게시글의 글쓴이가 맞는 지 확인
        if(!originalPost.getUser().equals(author)){
            throw new AccessDeniedException("이 게시글의 작성자만 수정할 수 있습니다");
        } else {
            //일단 먼저 dto에 있는 글과 내용만 먼저 수정.
            originalPost.updateText(postUpdateDto);
            //게시글에 있는 첨부파일 교체
            if (attachments != null) {
                try {
                    List<Attachment> newListOfAttachements = new ArrayList<>();
                    for (MultipartFile f : attachments) {
                        byte[] bytes = f.getBytes();
                        int z = 1;
                        String fileName = originalPost.getId() + "수정" + z + "_" + f.getOriginalFilename();
                        //로컬에 저장
                        Path path = Paths.get("C:/Users/Playdata/Desktop/tmp/", fileName);
                        Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                        //aws에 저장
                        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(fileName)
                                .build();

                        s3Client.putObject(putObjectRequest, RequestBody.fromFile(path));
                        String s3Url = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(fileName)).toExternalForm();

                        Attachment attachment = Attachment.builder()
                                .urlAdress(s3Url)
                                .fileName(fileName)
                                .post(originalPost)
                                .build();
                        newListOfAttachements.add(attachment);
                        attachmentRepository.save(attachment);
                    }
                    originalPost.updateAttachment(newListOfAttachements);

                } catch (IOException e) {
                    throw new RuntimeException("이미지 저장 실패");
                }
            }
        }
    }

//  6.게시글 삭제
    public void deleteById(Long id){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Post post = postRepository.findById(id).orElseThrow(()->new EntityNotFoundException("이미 존재하지 않는 게시글입니다"));
        String AuthorId = post.getUser().getLoginId();

        if(!userId.equals(AuthorId)){
            throw new AccessDeniedException("해당 글쓴이만 삭제할 수 있습니다");
        }
        postRepository.deleteById(id);
        User user = userRepository.findByLoginIdAndDelYN(userId,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 사용자입니다"));
        user.rankingPointUpdate(-20); //게시글 삭제시 다시 20점 회수
    }

//  7. 특정게시판 조회
    public Page<PostListDto> selectedBoard(Long id,Pageable pageable){
        Page<Post> postsOfBoard = postRepository.findAllByCategory_IdAndDelYN(id,DelYN.N,pageable);
        return postsOfBoard.map(p->p.toListDto());
    }

}
