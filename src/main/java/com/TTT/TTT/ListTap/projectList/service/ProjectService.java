package com.TTT.TTT.ListTap.projectList.service;

import com.TTT.TTT.ListTap.projectList.domain.PrimaryFeature;
import com.TTT.TTT.ListTap.projectList.domain.Project;
import com.TTT.TTT.ListTap.projectList.dtos.PrimaryFeatureSaveReq;
import com.TTT.TTT.ListTap.projectList.dtos.ProjectListRes;
import com.TTT.TTT.ListTap.projectList.dtos.ProjectSaveReq;
import com.TTT.TTT.ListTap.projectList.repository.PrimaryFeatureRepository;
import com.TTT.TTT.ListTap.projectList.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final PrimaryFeatureRepository primaryFeatureRepository;
    public ProjectService(ProjectRepository projectRepository, PrimaryFeatureRepository primaryFeatureRepository){
        this.projectRepository = projectRepository;
        this.primaryFeatureRepository = primaryFeatureRepository;
    }

//전체 프로젝트 목록 조회
    public Page<ProjectListRes> findAll(Pageable pageable){
       Page<Project> projects  =  projectRepository.findAll(pageable);
       return projects.map(p->p.toListResFromEntity(p.getPrimaryFeatureList().toString()));
    }

// 프로젝트 추가
    public void save(ProjectSaveReq projectSaveReq){
//     batch를 입력하지 않으면 0값이 들어오게 되어 기수가 0일때 기수 입력해야한다고 에러메세지 보냄.
        if(projectSaveReq.getBatch()==0){
            throw new IllegalArgumentException("Batch is required");
        }
//     일단 기능리스트를 제외한 프로젝트 객체 생성
      Project project = Project.builder().batch(projectSaveReq.getBatch()).projectType(projectSaveReq.getProjectType())
                                            .teamName(projectSaveReq.getTeamName()).serviceName(projectSaveReq.getServiceName())
                                            .link(projectSaveReq.getLink()).domain(projectSaveReq.getDomain()).build();

      //    프로젝트 추가할 때 해당 프로젝트에 구현된 기능에 대한 정보가 담긴 primaryFeatureSaveReqList가지고 엔티티인 기능리스트 만들기
        List<PrimaryFeature> primaryFeatureList =
            projectSaveReq.getPrimaryFeatureSaveReqList().stream().map(f->f.toEntity(project)).toList();

    for(PrimaryFeature p : primaryFeatureList){
        project.getPrimaryFeatureList().add(p);

    }
        projectRepository.save(project);

    }


}
