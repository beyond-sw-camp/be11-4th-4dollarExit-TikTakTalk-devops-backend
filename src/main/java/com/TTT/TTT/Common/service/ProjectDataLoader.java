package com.TTT.TTT.Common.service;

import com.TTT.TTT.ListTap.projectList.domain.PrimaryFeature;
import com.TTT.TTT.ListTap.projectList.domain.Project;
import com.TTT.TTT.ListTap.projectList.domain.ProjectType;
import com.TTT.TTT.ListTap.projectList.dtos.PrimaryFeatureSaveReq;
import com.TTT.TTT.ListTap.projectList.repository.ProjectRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectDataLoader implements CommandLineRunner {
    private final ProjectRepository projectRepository;
    private final ObjectMapper objectMapper;

    public ProjectDataLoader(ProjectRepository projectRepository, ObjectMapper objectMapper) {
        this.projectRepository = projectRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {

        InputStream is = getClass().getResourceAsStream("/projects.json");
        if (is != null) {
            List<ProjectSeedDto> projectSeedDtos = objectMapper.readValue(is, new TypeReference<List<ProjectSeedDto>>() {});
            List<Project> projects = projectSeedDtos.stream()
                    .map(dto -> {
                        // ProjectType는 JSON의 문자열(예:"DB", "FINAL", "BACKEND", "FRONTEND")를 enum으로 변환합니다.
                        Project project = Project.builder()
                                .batch(dto.getBatch())
                                .projectType(ProjectType.valueOf(dto.getProjectType()))
                                .teamName(dto.getTeamName())
                                .serviceName(dto.getServiceName())
                                .link(dto.getLink())
                                .domain(dto.getDomain())
                                .build();
                        // primaryFeatureSaveReqList가 존재하면 PrimaryFeature 엔티티로 변환
                        if (dto.getPrimaryFeatureSaveReqList() != null && !dto.getPrimaryFeatureSaveReqList().isEmpty()) {
                            List<PrimaryFeature> features = dto.getPrimaryFeatureSaveReqList().stream()
                                    .map(req -> req.toEntity(project))
                                    .collect(Collectors.toList());
                            project.setPrimaryFeatureList(features);
                        }
                        return project;
                    })
                    .collect(Collectors.toList());
            projectRepository.saveAll(projects);
            System.out.println("프로젝트 초기 데이터 로드 완료");
        } else {
            System.out.println("projects.json 파일을 찾을 수 없습니다.");
        }
    }

    // 내부 DTO 클래스: JSON 파일 구조와 동일한 필드를 가짐
    public static class ProjectSeedDto {
        private Integer batch;
        private String projectType;
        private String teamName;
        private String serviceName;
        private String link;
        private String domain;
        private List<PrimaryFeatureSaveReq> primaryFeatureSaveReqList;

        public Integer getBatch() {
            return batch;
        }
        public void setBatch(Integer batch) {
            this.batch = batch;
        }
        public String getProjectType() {
            return projectType;
        }
        public void setProjectType(String projectType) {
            this.projectType = projectType;
        }
        public String getTeamName() {
            return teamName;
        }
        public void setTeamName(String teamName) {
            this.teamName = teamName;
        }
        public String getServiceName() {
            return serviceName;
        }
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
        public String getLink() {
            return link;
        }
        public void setLink(String link) {
            this.link = link;
        }
        public String getDomain() {
            return domain;
        }
        public void setDomain(String domain) {
            this.domain = domain;
        }
        public List<PrimaryFeatureSaveReq> getPrimaryFeatureSaveReqList() {
            return primaryFeatureSaveReqList;
        }
        public void setPrimaryFeatureSaveReqList(List<PrimaryFeatureSaveReq> primaryFeatureSaveReqList) {
            this.primaryFeatureSaveReqList = primaryFeatureSaveReqList;
        }
    }
}
