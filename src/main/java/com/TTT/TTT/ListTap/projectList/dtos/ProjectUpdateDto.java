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
    private int batch;

    @NotNull(message = "프로젝트 유형은 필수 입력값입니다.")
    private ProjectType projectType;

    @NotBlank(message = "팀명은 필수 입력값입니다.")
    private String teamName;

    @NotBlank(message = "서비스명은 필수 입력값입니다.")
    private String serviceName;

    @NotBlank(message = "링크는 필수 입력값입니다.")
    private String link;

    @NotBlank(message = "프로젝트 주제는 필수 입력값입니다.")
    private String domain;

    // 기능 리스트도 수정 가능하도록 추가
    private List<PrimaryFeatureSaveReq> primaryFeatureSaveReqList;
}
