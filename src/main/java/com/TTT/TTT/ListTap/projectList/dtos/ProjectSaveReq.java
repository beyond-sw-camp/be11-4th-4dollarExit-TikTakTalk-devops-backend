package com.TTT.TTT.ListTap.projectList.dtos;

import com.TTT.TTT.ListTap.projectList.domain.ProjectType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectSaveReq {
    @NotNull//NotEmpty는 컬렉션,문자열,맵,배열에만 적용되므로 int에 적용되지않음
    private int batch;
    @NotNull //이넘타입에는 NotEmpty가 적용되지않음
    private ProjectType projectType;
    @NotEmpty
    private String teamName;
    @NotEmpty
    private String serviceName;
    @NotEmpty
    private String link;
    @NotEmpty
    private String domain;
    private List<PrimaryFeatureSaveReq> primaryFeatureSaveReqList = new ArrayList<>();


}
