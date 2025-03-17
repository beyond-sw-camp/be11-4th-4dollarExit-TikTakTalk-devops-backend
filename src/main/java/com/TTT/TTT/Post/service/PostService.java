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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;
    private final PostCategoryRepository postCategoryRepository;
    @Qualifier("likes")
    private final RedisTemplate<String,String> redisTemplate;
    private final S3Client s3Client;
    private final RedisServiceForViewCount redisServiceForViewCount;

    public PostService(PostRepository postRepository, UserRepository userRepository, AttachmentRepository attachmentRepository,
                       PostCategoryRepository postCategoryRepository, @Qualifier("likes") RedisTemplate<String, String> redisTemplate, S3Client s3Client,RedisServiceForViewCount redisServiceForViewCount) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.attachmentRepository = attachmentRepository;
        this.postCategoryRepository = postCategoryRepository;
        this.redisTemplate = redisTemplate;
        this.s3Client = s3Client;
        this.redisServiceForViewCount = redisServiceForViewCount;
    }

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    //  1.게시글 생성
    public void save(PostCreateDto dto, List<MultipartFile> attachments) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();
        User user = userRepository.findByLoginIdAndDelYN(loginId, DelYN.N)
                .orElseThrow(() -> new EntityNotFoundException("없는 아이디입니다"));
        PostCategory postCategory = postCategoryRepository.findById(dto.getPostCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("없는 게시판입니다"));

        // 첨부파일 속성 없이 먼저 게시글 저장
        Post post = Post.builder()
                .title(dto.getTitle())
                .contents(dto.getContents())
                .user(user)
                .delYN(DelYN.N)
                .category(postCategory)
                .build();
        postRepository.save(post);

        user.rankingPointUpdate(20); // 게시글 작성 시 랭킹 점수 20점 상승

        // 첨부파일 업로드 (로컬 저장 없이 S3에 직접 업로드)
        if (attachments != null) {
            List<Attachment> attachmentsOfPost = post.getAttachmentList();
            int z = 1; // 한 게시글에 여러 파일이 올라오면 이름 중복 방지

            for (MultipartFile f : attachments) {
                try {
                    String fileName = post.getId() + "_" + z + "_" + f.getOriginalFilename();
                    z++;

                    // S3에 직접 업로드
                    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(fileName)
                            .contentType(f.getContentType()) // MIME 타입 유지
                            .build();

                    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(f.getBytes()));

                    // S3 URL 생성
                    String s3Url = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(fileName)).toExternalForm();

                    // attachment 엔티티 생성하여 포스트의 첨부파일 리스트 속성에 저장
                    Attachment attachment = Attachment.builder()
                            .fileName(fileName)
                            .urlAdress(s3Url)
                            .post(post)
                            .build();
                    attachmentsOfPost.add(attachment);
                    attachmentRepository.save(attachment);
                } catch (IOException e) {
                    throw new RuntimeException("이미지 저장 실패", e);
                }
            }
        }
    }

//    2.게시글 조회
 public Page<PostAllListDto> findAll(Pageable pageable){
//     Page<Post> originalPostList =  postRepository.findAllByDelYN(DelYN.N,pageable);
     Page<Post> originalPostList = postRepository.findAllWithUser(DelYN.N,pageable);
     return originalPostList.map(p->p.toAllListDto(redisTemplate, redisServiceForViewCount.getViewCount(p.getId())));
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
        return originalPostList.map(p->p.toListDto(redisTemplate, redisServiceForViewCount.getViewCount(p.getId())));
    }

//    4.게시글 상세보기
    public PostDetailDto findById(Long id){
        Post post = postRepository.findByIdAndDelYN(id,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 게시글입니다"));
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        redisServiceForViewCount.increaseViewCount(post.getId(),userId); //해당 포스트에 대해 조회수 1증가시킴
        return post.toDetailDto(redisTemplate, redisServiceForViewCount.getViewCount(id));

    }


    //    5.게시글 수정
    public void updatePost(Long id, PostUpdateDto postUpdateDto, List<MultipartFile> attachments){
        String idOfAuthor = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByLoginIdAndDelYN(idOfAuthor, DelYN.N)
                .orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다"));
        Post originalPost = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("없는 게시글입니다"));

        // 수정하려는 로그인한 유저가 이 게시글의 글쓴이가 맞는 지 확인
        if (!originalPost.getUser().equals(author)) {
            throw new AccessDeniedException("이 게시글의 작성자만 수정할 수 있습니다");
        } else {
            // 글 내용 수정
            originalPost.updateText(postUpdateDto);

            // 첨부파일 업로드 (로컬 저장 없이 S3에 바로 업로드)
            if (attachments != null) {
                try {
                    List<Attachment> newListOfAttachments = new ArrayList<>();
                    int z = 1; // 파일 순서 관리

                    for (MultipartFile f : attachments) {
                        String fileName = originalPost.getId() + "수정" + z + "_" + f.getOriginalFilename();

                        // S3에 바로 업로드 (InputStream 사용)
                        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(fileName)
                                .contentType(f.getContentType())  // MIME 타입 지정
                                .build();

                        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(f.getBytes()));

                        // S3 URL 생성
                        String s3Url = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(fileName)).toExternalForm();

                        Attachment attachment = Attachment.builder()
                                .urlAdress(s3Url)
                                .fileName(fileName)
                                .post(originalPost)
                                .build();
                        newListOfAttachments.add(attachment);
                        attachmentRepository.save(attachment);

                        z++; // 다음 파일을 위해 증가
                    }

                    originalPost.updateAttachment(newListOfAttachments);
                } catch (IOException e) {
                    throw new RuntimeException("이미지 저장 실패", e);
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
        return postsOfBoard.map(p->p.toListDto(redisTemplate, redisServiceForViewCount.getViewCount(p.getId())));
    }


    // 8. 이미지 업로드
    public String dragImages(MultipartFile attachments) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();
        User user = userRepository.findByLoginIdAndDelYN(loginId, DelYN.N)
                .orElseThrow(() -> new EntityNotFoundException("없는 아이디입니다"));

        String returnUrl = "";

        try {
            // 고유한 파일명 생성 (UUID 사용)
            String fileName = UUID.randomUUID() + "_" + attachments.getOriginalFilename();

            // S3에 직접 업로드 (InputStream 사용)
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(attachments.getContentType()) // 파일 타입 유지
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(attachments.getBytes()));

            // S3 URL 생성
            returnUrl = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(fileName)).toExternalForm();

            // DB에 저장
            Attachment attachment = Attachment.builder()
                    .fileName(fileName)
                    .urlAdress(returnUrl)
                    .build();
            attachmentRepository.save(attachment);

        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }

        return returnUrl;
    }

//    9.게시글 전체 중에 상위 10개 인기순으로 조회
    public List<PostListDto> popularPost(){
      List<Post> top10List =  postRepository.findTop10ByOrderByLikesCountDesc();
     return top10List.stream().map(p->p.toListDto(redisTemplate, redisServiceForViewCount.getViewCount(p.getId()))).toList();
    }


//    post 전체개수 조회
    public Long totalCount() {
        Long postTotalCount = postRepository.count();
        return postTotalCount;
    }

    public List<PostListDto> likePost() {
//        현재날짜
        LocalDate today = LocalDate.now();
//        현재날짜 00시 00분
        LocalDateTime startOfDay = today.atStartOfDay();
//        현재날짜 23시59분 59초
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        List<Post> top12 = postRepository.findTop12ByCreatedTimeBetweenOrderByLikesCountDescCreatedTimeAsc(startOfDay, endOfDay);
        return top12.stream().map(p->p.toListDto(redisTemplate, redisServiceForViewCount.getViewCount(p.getId()))).toList();
    }
}
