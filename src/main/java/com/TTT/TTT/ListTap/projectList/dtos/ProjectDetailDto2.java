package com.TTT.TTT.ListTap.projectList.dtos;

import com.TTT.TTT.Comment.Dtos.CommentDetailDto;
import com.TTT.TTT.ListTap.projectList.domain.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProjectDetailDto2 {

    private Long id;
    private int batch;
    private ProjectType projectType;
    private String teamName;
    private String serviceName;
    private String link;
    private String domain;
    private String explanation;
    private String primaryFeatureList;
    private String userId;
    private Long userRealId;
    private String userName;
    private String authorNickName;
    private int likesCount;
    private int viewCount;
    private boolean liked;
    private List<CommentDetailDto> commentList;



}
