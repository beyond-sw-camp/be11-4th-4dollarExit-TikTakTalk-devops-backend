package com.TTT.TTT.ListTap.projectList.dtos;

import com.TTT.TTT.ListTap.projectList.domain.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectDetailRes {
    private int batch;
    private ProjectType projectType;
    private String teamName;
    private String serviceName;
    private String link;
    private String domain;
    private String primaryFeatureList;
}
