package com.TTT.TTT.ListTap.projectList.domain;

import com.TTT.TTT.Attachment.Domain.Attachment;
import com.TTT.TTT.Comment.Dtos.CommentDetailDto;
import com.TTT.TTT.Comment.domain.Comment;
import com.TTT.TTT.Common.domain.BaseTimeEntity;
import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.ListTap.projectList.dtos.*;
import com.TTT.TTT.User.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter

public class Project extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // null 허용
    @Setter
    @Column(nullable = true)
    private Integer batch;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectType projectType;

    @Column(nullable = false, length = 30)
    private String teamName;

    @Column(nullable = false, length = 30)
    private String serviceName;

    @Column(nullable = true)
    private String link;

    @Column(nullable = false)
    private String domain; //해당 프로젝트가 대강 어떤 서비스(카테고리 느낌)인지 설명하는 칼럼
//    @Builder.Default// 빌더패턴에서 필드를 초기화할때 @Builder.Default를 붙이지 않으면 무시된다.

    @OneToMany(mappedBy = "project")
    private List<Comment> commentList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    private int LikesCount;
    private int viewCount;

    //프로젝트에 대한 소개
    @Column(columnDefinition = "LONGTEXT")
    private String explanation;


    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PrimaryFeature> primaryFeatureList;

    // 🔥 ✅ ProjectSaveReq -> Project 변환 메서드 추가
    public static Project from(ProjectSaveReq req,User user) {
        return Project.builder()
                .explanation(req.getContents())
                .user(user)
                .batch(req.getBatch())
                .projectType(req.getProjectType())
                .teamName(req.getTeamName())
                .serviceName(req.getServiceName())
                .link(req.getLink())
                .domain(req.getDomain())
                .build();
    }

    //엔티티->목록조회용 Dto로 변환하는 메서드
    public ProjectListRes toListResFromEntity(RedisTemplate<String,String> redisTemplate,int count) {

        String likeCountKey = "project-" + this.id + "-likeCount"; // 레디스에서 좋아요 수를 가지고 와야하니까 좋아요 수를 넣을 때 만들었던 키와 동일하게 조립
        String likeCountValue = redisTemplate.opsForValue().get(likeCountKey); //get(key)를 통해 value값을 가지고 옴 //그런데 제일 처음에 좋아요가 없으면 null값이 오게 됨
        int likeCount = likeCountValue == null || likeCountValue.equals("0") ? 0 : Integer.parseInt(likeCountValue);
        System.out.println("좋아요" +likeCount);


        List<PrimaryFeatureRes> featureList = primaryFeatureList != null && !primaryFeatureList.isEmpty() ?
                primaryFeatureList.stream()
                        .map(f -> new PrimaryFeatureRes(f.getUtilityName()))
                        .toList() : new ArrayList<>();

        return ProjectListRes.builder()
                .id(this.id)
                .batch(this.batch)
                .teamName(this.teamName)
                .serviceName(this.serviceName)
                .link(this.link)
                .domain(this.domain)
                .likesCounts(likeCount)
                .viewCount(count)
                .commentCounts(this.commentList.size())
                .primaryFeatureList(featureList) // ✅ 새로운 DTO 리스트 적용
                .projectType(this.projectType)
                .build();
    }
    public ProjectDetailRes toDetailRes() {
        return ProjectDetailRes.builder()
                .id(this.id)
                .batch(this.batch)
                .projectType(this.projectType)
                .teamName(this.teamName)
                .serviceName(this.serviceName)
                .link(this.link)
                .domain(this.domain)
                .primaryFeatureList(
                        primaryFeatureList != null && !primaryFeatureList.isEmpty() ?
                        // PrimaryFeature의 utilityName들을 콤마로 연결 (필요에 따라 수정)
                        this.primaryFeatureList.stream()
                                .map(feature -> feature.getUtilityName())
                                .collect(Collectors.joining(", ")) :""
                )
                .build();
    }
    public void setLink(String link) {
        this.link = link != null && !link.trim().isEmpty() ? link.trim() : null; // 빈 문자열이면 null로 설정
    }
    public void setBatch(Integer batch) {
        this.batch = batch; // null 허용
    }



//    상세보기용 dto객체로 변환
   public ProjectDetailDto2 todetaildto2(RedisTemplate<String, String> redisTemplate,int count){
       List<CommentDetailDto> topLevelComments = new ArrayList<>();
// 게시글 상세보기를 할 때 일단, 대댓글이 아닌 원댓글들만 먼저 조회되게 해준다, 여기서 모든 this.commentList를 조회되게 하면 x
// 사용자가 게시글 볼 때 댓글-대댓글 계층적으로 보여줘야하기때문에 여기서 parent값이 없는 원댓글들만
// comment.toDetailDto로 변환해주면 거기서 재귀적으로 원댓글의 대댓글들을 변환해준다.

       if(this.commentList != null){
           for(Comment c : this.commentList) {
               if (c.getParent() == null && c.getDelYN()== DelYN.N) { //원댓글이면서 삭제되지않은 원댓글들
                   topLevelComments.add(c.toDetailDto());
               } else if(c.getParent()==null && c.getDelYN()==DelYN.Y){
                   topLevelComments.add(c.toDetailDto().pretendToDelete()); //만약에 원댓글이 삭제되어도 자식댓글들은 계속 볼 수 있게 하기 위해 여기서 자식 댓글들을 추가한다.(그런데 eager타입이어야 즉시 반영됨)
               }
           }
       }

       String likeCountKey = "project-" + this.id + "-likeCount";
       String likeUserKey = "project-" + this.id +"-likeUsers";
       String likeCountValue = redisTemplate.opsForValue().get(likeCountKey);
       int likesCount = likeCountValue==null||likeCountValue=="0" ? 0:Integer.parseInt(likeCountValue);
       String userId = SecurityContextHolder.getContext().getAuthentication().getName();
       boolean liked = redisTemplate.opsForSet().isMember(likeUserKey,userId);

        return ProjectDetailDto2.builder()
                .id(this.id)
                .batch(this.batch)
                .userId(this.user.getLoginId())
                .userRealId(this.user.getId())
                .userName(this.user.getName())
                .teamName(this.teamName)
                .serviceName(this.serviceName)
                .projectType(this.projectType)
                .domain(this.domain)
                .explanation(this.explanation)
                .likesCount(likesCount)
                .viewCount(count)
                .link(this.link)
                .liked(liked)
                .commentList(topLevelComments)
                .build();
   }
}
