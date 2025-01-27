package com.TTT.TTT.ListTap.projectList.controller;

import com.TTT.TTT.Common.CommonDto;
import com.TTT.TTT.ListTap.projectList.dtos.PrimaryFeatureSaveReq;
import com.TTT.TTT.ListTap.projectList.dtos.ProjectListRes;
import com.TTT.TTT.ListTap.projectList.dtos.ProjectSaveReq;
import com.TTT.TTT.ListTap.projectList.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("ttt/project")
public class ProjectController {

//    의존성 주입
    private final ProjectService projectService;
    public ProjectController(ProjectService projectService){
        this.projectService = projectService;
    }

//    전체 프로젝트 목록 조회
    @GetMapping("/list")
    public ResponseEntity<?> findByAll(@PageableDefault(size=20, sort ="batch", direction = Sort.Direction.DESC)Pageable pageable){
       Page<ProjectListRes> pages =  projectService.findAll(pageable);
       return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "success",pages),HttpStatus.OK);
    }

//   프로젝트 추가
    @PostMapping("/create")
    public ResponseEntity<?> save(@RequestBody @Valid ProjectSaveReq projectSaveReq){
        projectService.save(projectSaveReq);
        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(), "success","success"),HttpStatus.CREATED);
    }
}
