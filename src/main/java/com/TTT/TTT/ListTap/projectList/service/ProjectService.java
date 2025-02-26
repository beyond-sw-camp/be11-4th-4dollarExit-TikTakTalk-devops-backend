package com.TTT.TTT.ListTap.projectList.service;

import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.ListTap.projectList.domain.PrimaryFeature;
import com.TTT.TTT.ListTap.projectList.domain.Project;
import com.TTT.TTT.ListTap.projectList.dtos.*;
import com.TTT.TTT.ListTap.projectList.repository.PrimaryFeatureRepository;
import com.TTT.TTT.ListTap.projectList.repository.ProjectRepository;
import com.TTT.TTT.Post.service.RedisServiceForViewCount;
import com.TTT.TTT.User.UserRepository.UserRepository;
import com.TTT.TTT.User.domain.User;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectService {
    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final PrimaryFeatureRepository primaryFeatureRepository;
    @Qualifier("project")
    private final RedisTemplate<String,String> redisTemplate;
    private final RedisServiceForViewCount redisServiceForViewCount;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, PrimaryFeatureRepository primaryFeatureRepository, @Qualifier("project") RedisTemplate<String, String> redisTemplate, RedisServiceForViewCount redisServiceForViewCount) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.primaryFeatureRepository = primaryFeatureRepository;
        this.redisTemplate = redisTemplate;
        this.redisServiceForViewCount = redisServiceForViewCount;
    }

    // 전체 프로젝트 목록 조회(페이지)
    public Page<ProjectListRes> findAll(Pageable pageable) {
        Page<Project> projects = projectRepository.findAll(pageable);
        return projects.map(p->p.toListResFromEntity(redisTemplate,redisServiceForViewCount.getViewCountForProject(p.getId())));
    }
    //전체 프로젝트 목록
    public List<ProjectListRes> findAllAll() {
        List<Project> projects = projectRepository.findAll();
        return projects.stream().map(p->p.toListResFromEntity(redisTemplate,redisServiceForViewCount.getViewCountForProject(p.getId()))).toList();
    }

    // 프로젝트 추가
    public void save(ProjectSaveReq projectSaveReq) {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByLoginIdAndDelYN(loginId, DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 사용자입니다"));

        logger.info("📌 [LOG] 저장할 프로젝트 데이터: {}", projectSaveReq);
        // ✅ primaryFeatureSaveReqList가 null이거나 비어 있는 경우, 로그 추가
        if (projectSaveReq.getPrimaryFeatureSaveReqList() == null) {
            logger.warn("⚠️ [LOG] primaryFeatureSaveReqList가 null임! 빈 리스트로 초기화");
            projectSaveReq.setPrimaryFeatureSaveReqList(new ArrayList<>()); // 기본값 설정
        }
        // ✅ 프로젝트 저장
        final Project savedProject = projectRepository.save(Project.from(projectSaveReq,user));
        if (projectSaveReq.getPrimaryFeatureSaveReqList().isEmpty()) {
            logger.warn("⚠️ [LOG] primaryFeatureSaveReqList가 비어 있음!");
        } else {
            logger.info("✅ [LOG] primaryFeatureSaveReqList 데이터 있음: {}", projectSaveReq.getPrimaryFeatureSaveReqList());
        }

        // ✅ 기능 리스트 저장
        if (!projectSaveReq.getPrimaryFeatureSaveReqList().isEmpty()) {
            List<PrimaryFeature> primaryFeatureList = projectSaveReq.getPrimaryFeatureSaveReqList()
                    .stream()
                    .map(req -> req.toEntity(savedProject))
                    .filter(Objects::nonNull)
                    .toList();
            try {
                primaryFeatureRepository.saveAll(primaryFeatureList);
            } catch (Exception e) {
                logger.error("기능 저장 실패: ", e);
                throw new RuntimeException("기능 저장에 실패했습니다: " + e.getMessage());
            }
        }
    }

    // 검색 기능 추가
    public Page<ProjectListRes> findProjects(ProjectSearchDto projectSearchDto, Pageable pageable) {
        Specification<Project> specification = (root, query, criteriaBuilder) -> {
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
            // 🔍 프로젝트 유형 검색 (정확한 일치 조건 사용)
            if (projectSearchDto.getProjectType() != null && !projectSearchDto.getProjectType().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("projectType"), projectSearchDto.getProjectType()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Page<Project> projects = projectRepository.findAll(specification, pageable);
        return projects.map(p->p.toListResFromEntity(redisTemplate,redisServiceForViewCount.getViewCountForProject(p.getId())));
    }

    // ✅ 프로젝트 수정
    public void updateProject(Long id, ProjectUpdateDto updateDto) {
        logger.info("📌 [LOG] 프로젝트 수정 요청: ID={}, Data={}", id, updateDto);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 프로젝트가 존재하지 않습니다. ID=" + id));

        // 데이터 유효성 검증 (batch는 null 허용)
        if (updateDto.getProjectType() == null) {
            throw new IllegalArgumentException("프로젝트 유형은 필수 입력값입니다.");
        }
        if (updateDto.getTeamName() == null || updateDto.getTeamName().trim().isEmpty()) {
            throw new IllegalArgumentException("팀명은 필수 입력값입니다.");
        }
        if (updateDto.getServiceName() == null || updateDto.getServiceName().trim().isEmpty()) {
            throw new IllegalArgumentException("서비스명은 필수 입력값입니다.");
        }
        if (updateDto.getDomain() == null || updateDto.getDomain().trim().isEmpty()) {
            throw new IllegalArgumentException("도메인은 필수 입력값입니다.");
        }

        // 프로젝트 기본 정보 업데이트 (null 허용)
        project.setBatch(updateDto.getBatch()); // null 허용
        project.setProjectType(updateDto.getProjectType());
        project.setTeamName(updateDto.getTeamName().trim());
        project.setServiceName(updateDto.getServiceName().trim());
        project.setLink(updateDto.getLink() != null && !updateDto.getLink().trim().isEmpty() ? updateDto.getLink().trim() : null); // null 허용
        project.setDomain(updateDto.getDomain().trim());

        // ✅ 기능 리스트 업데이트 (삭제 후 새로 추가, null이면 빈 리스트로 처리)
        primaryFeatureRepository.deleteAllByProject(project);

        List<PrimaryFeatureSaveReq> featureSaveReqList = updateDto.getPrimaryFeatureSaveReqList() != null ?
                updateDto.getPrimaryFeatureSaveReqList() : new ArrayList<>();

        if (!featureSaveReqList.isEmpty()) {
            List<PrimaryFeature> primaryFeatureList = featureSaveReqList.stream()
                    .map(req -> {
                        if (req == null || req.getUtilityName() == null || req.getUtilityName().trim().isEmpty()) {
                            return null; // 유효하지 않은 기능은 무시
                        }
                        return req.toEntity(project);
                    })
                    .filter(Objects::nonNull) // null 제거
                    .collect(Collectors.toList());

            try {
                primaryFeatureRepository.saveAll(primaryFeatureList);
            } catch (Exception e) {
                logger.error("기능 저장 실패: ", e);
                throw new RuntimeException("기능 저장에 실패했습니다: " + e.getMessage(), e);
            }
        }

        try {
            projectRepository.save(project);
            logger.info("📌 [LOG] 프로젝트 수정 성공: ID={}", id);
        } catch (Exception e) {
            logger.error("프로젝트 저장 실패: ", e);
            throw new RuntimeException("프로젝트 저장에 실패했습니다: " + e.getMessage(), e);
        }
    }

    // ✅ 프로젝트 삭제
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다."));
        projectRepository.delete(project);
    }

    public ProjectDetailRes getProjectDetail(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 프로젝트가 존재하지 않습니다."));
        return project.toDetailRes();
    }

    // 진영추가 프로젝트 상세보기
    public ProjectDetailDto2 projectDetailSee(Long id){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        redisServiceForViewCount.increaseViewCountForProject(id,userId);
        Project project = projectRepository.findById(id).orElseThrow(()->new EntityNotFoundException("없는 프로젝트입니다"));
        return project.todetaildto2(redisTemplate,redisServiceForViewCount.getViewCountForProject(project.getId()));

    }

    //진영추가
    public List<ProjectListRes> findByFeature(String featureName) {
        List<Project> projects = projectRepository.findByPrimaryFeatureList_UtilityName(featureName);
        return projects.stream()
                .map(p -> p.toListResFromEntity(redisTemplate,redisServiceForViewCount.getViewCountForProject(p.getId())))
                .collect(Collectors.toList());
    }



}