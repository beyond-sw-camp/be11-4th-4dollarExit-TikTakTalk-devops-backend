package com.TTT.TTT.Post.domain;

import com.TTT.TTT.Attachment.Domain.Attachment;
import com.TTT.TTT.Comment.Dtos.CommentDetailDto;
import com.TTT.TTT.Comment.domain.Comment;
import com.TTT.TTT.Likes.domain.Likes;
import com.TTT.TTT.Post.dtos.PostAllListDto;
import com.TTT.TTT.Post.dtos.PostDetailDto;
import com.TTT.TTT.Post.dtos.PostListDto;
import com.TTT.TTT.Post.dtos.PostUpdateDto;
import com.TTT.TTT.Common.domain.BaseTimeEntity;
import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.PostCategory.domain.PostCategory;
import com.TTT.TTT.User.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Formula;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
public class Post extends BaseTimeEntity {
    //    아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //  작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    //  글 제목
    @Column(nullable = false, length = 100)
    private String title;
    //  글 내용
    private String contents;
    //  이미지(첨부파일)
    @OneToMany(mappedBy = "post", orphanRemoval = true)
    @Builder.Default
    private List<Attachment> attachmentList = new ArrayList<>();
    //  댓글(//eager설정은 원댓글 삭제처리시 대댓글들은 여전히 보이게 설정하려는데, 원댓글의 삭제칼럼이 y로 변경이 되면 이걸 늦게 반영이 되어 대댓글도 삭제처리된 것처럼 한동안 보이게 되어 eager설정으로 두었습니다)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @BatchSize(size = 20) // n+1문제를 해결하기 위해 단 어노테이션.연관 관계에 적용가능한 것으로 jpa가 in 절을 활용하여 한꺼번에 데이터를 가져오도록하는 개념, Batchsize는 many-to-one관계에서는 사용할 수 없다.
    private List<Comment> commentList = new ArrayList<>();
    //  게시판 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private PostCategory category;
    //  삭제 여부
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DelYN delYN = DelYN.N;
    //조회수 추가
    private Integer viewCount;
    //좋아요 개수(각 게시글의 좋아요 개수를 계산 하는 서브 쿼리)
    @Formula("(SELECT COUNT(*) FROM likes l WHERE l.post_id = id )")
    private int likesCount;



    public void deletePost() {
        this.delYN = DelYN.Y;
    }

    //게시글 수정
    public void updateText(PostUpdateDto postUpdateDto) {
        this.title = postUpdateDto.getTitle();
        this.contents = postUpdateDto.getContents();
    }

    //게시글 수정(jpa가 셋 자료 구조의 변경을 인지하기 위해서는 .clear로 싹 삭제하고 addAll을 해야 인지)
    public void updateAttachment(List<Attachment> newAttachments) {
        this.attachmentList.clear();
        this.attachmentList.addAll(newAttachments);
    }
    //조회수 수정
    public void updateViewCounts(int count){
        this.viewCount = count;
    }


// DTO변환 메서드

    public PostListDto toListDto(RedisTemplate<String, String> redisTemplate, int viewCount) {
        String likeCountKey = "post-" + this.id + "-likeCount";
        String likeCountValue = redisTemplate.opsForValue().get(likeCountKey);
        int likesCount = likeCountValue == null || likeCountValue.equals("0") ? 0 : Integer.parseInt(likeCountValue);
        return PostListDto.builder()
                .postId(this.id)
                .title(this.title)
                .createdTime(this.getCreatedTime())
                .AuthorNickName(this.user.getNickName())
                .AuthorImage(this.user.getProfileImagePath())
                .countOfComment(this.commentList.size())
                .likesCount(likesCount)
                .viewCount(viewCount)
                .contents(this.contents)
                .AuthorRankingPoint(this.user.getRankingPoint())
                .categoryName(this.category.getCategoryName())
                .build();
    }

    public PostAllListDto toAllListDto(RedisTemplate<String, String> redisTemplate, int viewCount) {
        String likeCountKey = "post-" + this.id + "-likeCount"; // 레디스에서 좋아요 수를 가지고 와야하니까 좋아요 수를 넣을 때 만들었던 키와 동일하게 조립
        String likeCountValue = redisTemplate.opsForValue().get(likeCountKey); //get(key)를 통해 value값을 가지고 옴 //그런데 제일 처음에 좋아요가 없으면 null값이 오게 됨
        int likeCount = likeCountValue == null || likeCountValue.equals("0") ? 0 : Integer.parseInt(likeCountValue); //레디스는 숫자형이 없이 문자열이다. 레디스에서 0값은 null이다

        return PostAllListDto.builder()
                .postId(this.id)
                .title(this.title)
                .contents(this.contents)
                .categoryName(this.category.getCategoryName())
                .createdTime(this.getCreatedTime())
                .AuthorNickName(this.user.getNickName())
                .AuthorImage(this.user.getProfileImagePath())
                .countOfComment(this.commentList.size())
                .AuthorRankingPoint(this.user.getRankingPoint())
                .LikesCount(likeCount)
                .viewCount(viewCount)
                .build();
    }

    public PostDetailDto toDetailDto(RedisTemplate<String,String> redisTemplate,int viewCount){
        List<CommentDetailDto> topLevelComments = new ArrayList<>();
// 게시글 상세보기를 할 때 일단, 대댓글이 아닌 원댓글들만 먼저 조회되게 해준다, 여기서 모든 this.commentList를 조회되게 하면 x
// 사용자가 게시글 볼 때 댓글-대댓글 계층적으로 보여줘야하기때문에 여기서 parent값이 없는 원댓글들만
// comment.toDetailDto로 변환해주면 거기서 재귀적으로 원댓글의 대댓글들을 변환해준다.

        if(this.commentList != null){
            for(Comment c : this.commentList) {
                if (c.getParent() == null && c.getDelYN()==DelYN.N) { //원댓글이면서 삭제되지않은 원댓글들
                    topLevelComments.add(c.toDetailDto());
                } else if(c.getParent()==null && c.getDelYN()==DelYN.Y){
                    topLevelComments.add(c.toDetailDto().pretendToDelete()); //만약에 원댓글이 삭제되어도 자식댓글들은 계속 볼 수 있게 하기 위해 여기서 자식 댓글들을 추가한다.(그런데 eager타입이어야 즉시 반영됨)
                }
            }
        }
        List<String> attachmentUrls = new ArrayList<>();
        if(this.attachmentList != null){
            for(Attachment a : attachmentList){
                attachmentUrls.add(a.getUrlAdress());
            }
        }

        String likeCountKey = "post-" + this.id + "-likeCount";
        String likeUserKey = "post-" + this.id +"-likeUsers";
        String likeCountValue = redisTemplate.opsForValue().get(likeCountKey);
        int likesCount = likeCountValue==null||likeCountValue=="0" ? 0:Integer.parseInt(likeCountValue);
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean liked = redisTemplate.opsForSet().isMember(likeUserKey,userId);

        return PostDetailDto.builder()
                .postId(this.id)
                .postUserId(this.user.getId())
                .title(this.title)
                .contents(this.contents)
                .authorId(this.user.getLoginId())
                .authorNickName(this.user.getNickName())
                .AuthorImage(this.user.getProfileImagePath())
                .rankingPointOfAuthor(this.user.getRankingPoint())
                .likesCount(likesCount)
                .liked(liked)
                .viewCount(viewCount)
                .attachmentsUrl(attachmentUrls)
                .commentList(topLevelComments)
                .createdTime(this.getCreatedTime())
                .categoryName(this.category.getCategoryName())
                .build();
    }

}