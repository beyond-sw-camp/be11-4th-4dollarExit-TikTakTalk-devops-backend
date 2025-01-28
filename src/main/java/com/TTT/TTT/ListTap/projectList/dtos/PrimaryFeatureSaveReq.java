package com.TTT.TTT.ListTap.projectList.dtos;

import com.TTT.TTT.ListTap.projectList.domain.PrimaryFeature;
import com.TTT.TTT.ListTap.projectList.domain.Project;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PrimaryFeatureSaveReq {

    @Column(nullable = false)
    private String utilityName;
    private Long projectId;



//    엔티티변환 메서드
    public PrimaryFeature toEntity(Project project){
        return  PrimaryFeature.builder().utilityName(this.utilityName).project(project).build();
    }
}
