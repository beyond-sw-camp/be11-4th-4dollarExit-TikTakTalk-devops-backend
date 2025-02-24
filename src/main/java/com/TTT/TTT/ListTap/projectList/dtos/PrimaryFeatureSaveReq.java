package com.TTT.TTT.ListTap.projectList.dtos;

import com.TTT.TTT.ListTap.projectList.domain.PrimaryFeature;
import com.TTT.TTT.ListTap.projectList.domain.Project;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PrimaryFeatureSaveReq {
    private String utilityName;

    // ✅ 엔티티 변환 메서드
    public PrimaryFeature toEntity(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        return PrimaryFeature.builder()
                .project(project)
                .utilityName(utilityName)
                .build();
    }
}
