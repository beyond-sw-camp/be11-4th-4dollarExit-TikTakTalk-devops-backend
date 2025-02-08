package com.TTT.TTT.ListTap.projectList.domain;

import com.TTT.TTT.Common.domain.BaseTimeEntity;
import com.TTT.TTT.ListTap.projectList.dtos.ProjectListRes;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Project extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private int batch;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectType projectType;
    @Column(nullable = false, length = 30)
    private String teamName;
    @Column(nullable = false, length = 30)
    private String serviceName;
    @Column(nullable = false)
    private String link;
    @Column(nullable = false)
    private String domain; //해당 프로젝트가 대강 어떤 서비스(카테고리 느낌)인지 설명하는 칼럼
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @Builder.Default// 빌더패턴에서 필드를 초기화할때 @Builder.Default를 붙이지 않으면 무시된다.
    private List<PrimaryFeature> primaryFeatureList= new ArrayList<>();


    //엔티티->목록조회용 Dto로 변환하는 메서드
    public ProjectListRes toListResFromEntity (String featureList){
        return ProjectListRes.builder().batch(this.batch).teamName(this.teamName).serviceName(this.serviceName)
                                        .link(this.link).domain(this.domain)
                                       .primaryFeatureList(featureList).projectType(this.projectType).build();
    }


}
