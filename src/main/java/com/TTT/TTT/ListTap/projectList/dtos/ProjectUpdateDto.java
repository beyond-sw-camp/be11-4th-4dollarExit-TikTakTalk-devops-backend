package com.TTT.TTT.ListTap.projectList.dtos;

import com.TTT.TTT.ListTap.projectList.domain.ProjectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectUpdateDto {
    @NotNull(message = "batch는 필수 입력값입니다.")
    private Integer batch; // null 허용

    @NotNull(message = "프로젝트 유형은 필수 입력값입니다.")
    private ProjectType projectType;

    @NotBlank(message = "팀명은 필수 입력값입니다.")
    private String teamName;

    @NotBlank(message = "서비스명은 필수 입력값입니다.")
    private String serviceName;

    private String link; // null 허용, @NotBlank 제거

    @NotBlank(message = "프로젝트 주제는 필수 입력값입니다.")
    private String domain;

    private List<PrimaryFeatureSaveReq> primaryFeatureSaveReqList;
}