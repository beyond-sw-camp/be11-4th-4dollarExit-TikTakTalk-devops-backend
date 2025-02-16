package com.TTT.TTT.Comment.domain;

import com.TTT.TTT.Comment.Dtos.CommentDetailDto;

import com.TTT.TTT.Common.domain.BaseTimeEntity;
import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.Post.domain.Post;

import com.TTT.TTT.User.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Comment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "최소 2자이상 최대 1000자 까지 입력이 가능합니다.")
    @Size(min = 2, max = 1000, message = "최소 2자이상 최대 1000자 까지 입력이 가능합니다")
    private String contents;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private DelYN delYN=DelYN.N;
//   대댓글기능을 구현하기 위해선 댓글에 parent필드가 필요하다. 이 필드가 null이면 대댓글이 달리지 않은 그냥 댓글이라는 뜻
//   여러 댓글들이 하나의 부모댓글을 가지니까
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;
//    하나의 부모댓글에 여러 댓글이 달리니까 onetomany
    @OneToMany(mappedBy = "parent" , cascade = CascadeType.ALL)
    @Builder.Default
    private List<Comment> childs= new ArrayList<>();




    public void update(String contents){
        this.contents = contents;
    }

    public void delete(){
        this.delYN =DelYN.Y;
    }



    public CommentDetailDto toDetailDto(){
        List<CommentDetailDto> childDetailList = new ArrayList<>();

        for(Comment childC : this.childs) {
            if(childC.getDelYN()==DelYN.N) {
                childDetailList.add(childC.toDetailDto()); //재귀호출!!
            } else if(childC.getDelYN()==DelYN.Y){
                childDetailList.add(childC.toDetailDto().pretendToDelete()); //삭제된것 처럼 처리하여 또 대댓글도 보일 수 있도록
            }

        }
                return CommentDetailDto.builder()
                        .commentId(this.id)
                        .contents(this.delYN == DelYN.Y ? "[삭제된 댓글입니다]" : this.contents) // 삭제처리한 댓글이라면 [삭제된 댓글]표시 아니면 원래 내용표시
                        .profileImageOfCommentAuthor(this.user.getProfileImagePath())
                        .loginIdOfCommentAuthor(this.user.getLoginId())
                        .nickNameOfCommentAuthor(this.user.getNickName())
                        .rankingPointOfCommentAuthor(this.user.getRankingPoint())
                        .childCommentList(childDetailList)
                        .createdTime(this.getCreatedTime())
                        .build();
            }

}
