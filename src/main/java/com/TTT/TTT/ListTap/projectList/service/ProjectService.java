package com.TTT.TTT.ListTap.projectList.service;

import com.TTT.TTT.ListTap.projectList.domain.PrimaryFeature;
import com.TTT.TTT.ListTap.projectList.domain.Project;
import com.TTT.TTT.ListTap.projectList.dtos.ProjectListRes;
import com.TTT.TTT.ListTap.projectList.dtos.ProjectSaveReq;
import com.TTT.TTT.ListTap.projectList.dtos.ProjectSearchDto;
import com.TTT.TTT.ListTap.projectList.repository.PrimaryFeatureRepository;
import com.TTT.TTT.ListTap.projectList.repository.ProjectRepository;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final PrimaryFeatureRepository primaryFeatureRepository;

    public ProjectService(ProjectRepository projectRepository, PrimaryFeatureRepository primaryFeatureRepository) {
        this.projectRepository = projectRepository;
        this.primaryFeatureRepository = primaryFeatureRepository;
    }

    //전체 프로젝트 목록 조회
    public Page<ProjectListRes> findAll(Pageable pageable) {
        Page<Project> projects = projectRepository.findAll(pageable);
        return projects.map(Project::toListResFromEntity);
    }

    // 프로젝트 추가
    public void save(ProjectSaveReq projectSaveReq) {
        System.out.println("📌 [LOG] 저장할 프로젝트 데이터: " + projectSaveReq);
        // ✅ primaryFeatureSaveReqList가 null이거나 비어 있는 경우, 로그 추가
        if (projectSaveReq.getPrimaryFeatureSaveReqList() == null) {
            System.out.println("⚠️ [LOG] primaryFeatureSaveReqList가 null임! 빈 리스트로 초기화");
            projectSaveReq.setPrimaryFeatureSaveReqList(new ArrayList<>()); //기본값 설정
        }

        // ✅ 프로젝트 저장
        final Project savedProject = projectRepository.save(Project.from(projectSaveReq));
        if (projectSaveReq.getPrimaryFeatureSaveReqList().isEmpty()){
            System.out.println("⚠️ [LOG] primaryFeatureSaveReqList가 비어 있음!");
        }
        else {
            System.out.println("✅ [LOG] primaryFeatureSaveReqList 데이터 있음: " + projectSaveReq.getPrimaryFeatureSaveReqList());
        }
        // ✅ 기능 리스트 저장
        if (!projectSaveReq.getPrimaryFeatureSaveReqList().isEmpty()) {
            List<PrimaryFeature> primaryFeatureList = projectSaveReq.getPrimaryFeatureSaveReqList()
                    .stream()
                    .map(req -> req.toEntity(savedProject)) // ✅ `savedProject`를 final로 변경하여 람다에서 사용 가능
                    .toList();
            primaryFeatureRepository.saveAll(primaryFeatureList);
        }

    }

    // 검색 기능 추가
    public Page<ProjectListRes> findProjects(ProjectSearchDto projectSearchDto, Pageable pageable) {
        Specification<Project> specification = new Specification<Project>() {
            @Override
            public Predicate toPredicate(Root<Project> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();

                // 🔍 기수(batch) 검색
                if (projectSearchDto.getBatch() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("batch"), projectSearchDto.getBatch()));
                }
                // 🔍 팀명 검색 (like 연산 사용)
                if (projectSearchDto.getTeamName() != null && !projectSearchDto.getTeamName().isEmpty()) {
                    predicates.add(criteriaBuilder.like(root.get("teamName"), "%" + projectSearchDto.getTeamName() + "%"));
                }
                // 🔍 서비스명 검색 (like 연산 사용)
                if (projectSearchDto.getServiceName() != null && !projectSearchDto.getServiceName().isEmpty()) {
                    predicates.add(criteriaBuilder.like(root.get("serviceName"), "%" + projectSearchDto.getServiceName() + "%"));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };

        Page<Project> projects = projectRepository.findAll(specification, pageable);
        return projects.map(Project::toListResFromEntity);
    }
}
