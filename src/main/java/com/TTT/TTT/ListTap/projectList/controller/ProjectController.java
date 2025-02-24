package com.TTT.TTT.ListTap.projectList.controller;

import com.TTT.TTT.Common.dtos.CommonDto;
import com.TTT.TTT.ListTap.projectList.domain.ProjectType;
import com.TTT.TTT.ListTap.projectList.dtos.ProjectListRes;
import com.TTT.TTT.ListTap.projectList.dtos.ProjectSaveReq;
import com.TTT.TTT.ListTap.projectList.dtos.ProjectSearchDto;
import com.TTT.TTT.ListTap.projectList.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("ttt/project")
public class ProjectController {

    //    의존성 주입
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    //    전체 프로젝트 목록 조회
    @GetMapping("/list")
    public ResponseEntity<?> findByAll(@PageableDefault(size = 20, sort = "batch", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProjectListRes> pages = projectService.findAll(pageable);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "success", pages), HttpStatus.OK);
    }

    //   프로젝트 추가
    @PostMapping("/create")
    public ResponseEntity<?> save(@RequestBody @Valid ProjectSaveReq projectSaveReq) {

        // ✅ primaryFeatureSaveReqList가 null이면 빈 리스트로 변환
        if (projectSaveReq.getPrimaryFeatureSaveReqList() == null) {
            projectSaveReq.setPrimaryFeatureSaveReqList(new ArrayList<>());
            System.out.println("✅ [LOG] primaryFeatureSaveReqList가 null이어서 빈 리스트로 초기화됨.");
        }
        projectService.save(projectSaveReq);
        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(), "success", "success"), HttpStatus.CREATED);
    }

    //    검색
    @GetMapping("/find")
    public ResponseEntity<?> searchProjects(ProjectSearchDto projectSearchDto, Pageable pageable) {
        Page<ProjectListRes> pages = projectService.findProjects(projectSearchDto, pageable);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "search success", pages), HttpStatus.OK);
    }
//  프로젝트유형선택 유형 불러오기
    @GetMapping("/types")
    public ResponseEntity<List<String>> getProjectTypes() {
        List<String> projectTypes = Arrays.stream(ProjectType.values())
                .map(Enum::name)
                .toList();
        return ResponseEntity.ok(projectTypes);
    }
}
