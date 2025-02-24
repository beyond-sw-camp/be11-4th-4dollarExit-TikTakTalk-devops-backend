package com.TTT.TTT.ListTap.projectList.dtos;

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
public class ProjectListRes {
     private Long id;
     private int batch;
     private ProjectType projectType;
     private String teamName;
     private String serviceName;
     private String link;
     private String domain;
     private List<PrimaryFeatureRes> primaryFeatureList;
    }

