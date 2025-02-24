package com.TTT.TTT.ListTap.projectList.domain;

import com.TTT.TTT.Common.domain.BaseTimeEntity;
import com.TTT.TTT.ListTap.projectList.dtos.PrimaryFeatureRes;
import com.TTT.TTT.ListTap.projectList.dtos.ProjectDetailRes;
import com.TTT.TTT.ListTap.projectList.dtos.ProjectListRes;
import com.TTT.TTT.ListTap.projectList.dtos.ProjectSaveReq;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter

public class Project extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // null 허용
    @Setter
    @Column(nullable = true)
    private Integer batch;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectType projectType;

    @Column(nullable = false, length = 30)
    private String teamName;

    @Column(nullable = false, length = 30)
    private String serviceName;

    @Column(nullable = true)
    private String link;

    @Column(nullable = false)
    private String domain; //해당 프로젝트가 대강 어떤 서비스(카테고리 느낌)인지 설명하는 칼럼
//    @Builder.Default// 빌더패턴에서 필드를 초기화할때 @Builder.Default를 붙이지 않으면 무시된다.

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PrimaryFeature> primaryFeatureList;

    // 🔥 ✅ ProjectSaveReq -> Project 변환 메서드 추가
    public static Project from(ProjectSaveReq req) {
        return Project.builder()
                .batch(req.getBatch())
                .projectType(req.getProjectType())
                .teamName(req.getTeamName())
                .serviceName(req.getServiceName())
                .link(req.getLink())
                .domain(req.getDomain())
                .build();
    }

    //엔티티->목록조회용 Dto로 변환하는 메서드
    public ProjectListRes toListResFromEntity() {
        List<PrimaryFeatureRes> featureList = primaryFeatureList != null && !primaryFeatureList.isEmpty() ?
                primaryFeatureList.stream()
                        .map(f -> new PrimaryFeatureRes(f.getUtilityName()))
                        .toList() : new ArrayList<>();

        return ProjectListRes.builder()
                .id(this.id)
                .batch(this.batch)
                .teamName(this.teamName)
                .serviceName(this.serviceName)
                .link(this.link)
                .domain(this.domain)
                .primaryFeatureList(featureList) // ✅ 새로운 DTO 리스트 적용
                .projectType(this.projectType)
                .build();
    }
    public ProjectDetailRes toDetailRes() {
        return ProjectDetailRes.builder()
                .id(this.id)
                .batch(this.batch)
                .projectType(this.projectType)
                .teamName(this.teamName)
                .serviceName(this.serviceName)
                .link(this.link)
                .domain(this.domain)
                .primaryFeatureList(
                        primaryFeatureList != null && !primaryFeatureList.isEmpty() ?
                        // PrimaryFeature의 utilityName들을 콤마로 연결 (필요에 따라 수정)
                        this.primaryFeatureList.stream()
                                .map(feature -> feature.getUtilityName())
                                .collect(Collectors.joining(", ")) :""
                )
                .build();
    }
    public void setLink(String link) {
        this.link = link != null && !link.trim().isEmpty() ? link.trim() : null; // 빈 문자열이면 null로 설정
    }
    public void setBatch(Integer batch) {
        this.batch = batch; // null 허용
    }
}
