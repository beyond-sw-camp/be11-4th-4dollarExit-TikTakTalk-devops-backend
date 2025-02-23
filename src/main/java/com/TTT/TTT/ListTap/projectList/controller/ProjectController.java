package com.TTT.TTT.ListTap.projectList.controller;

import com.TTT.TTT.Common.dtos.CommonDto;
import com.TTT.TTT.ListTap.projectList.domain.ProjectType;
import com.TTT.TTT.ListTap.projectList.dtos.*;
import com.TTT.TTT.ListTap.projectList.service.ProjectService;
import com.TTT.TTT.User.UserService.UserService;
import com.TTT.TTT.User.domain.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("ttt/project")
public class ProjectController {

    //    의존성 주입
    private final ProjectService projectService;
    private final UserService userService;
    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    @GetMapping("/role")
    public ResponseEntity<?> getUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName(); // 현재 로그인한 사용자의 ID 가져오기

        User user =  userService.findByLoginId(loginId);

        Map<String, String> response = new HashMap<>();
        response.put("role", user.getRole().toString());

        return ResponseEntity.ok(response);
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

    @DeleteMapping("delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")  // ✅ ADMIN 권한만 삭제 가능
    public ResponseEntity<String> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok("삭제 완료");
    }

    @PutMapping("update/{id}")
    @PreAuthorize("hasRole('ADMIN')")  // ✅ ADMIN 권한만 수정 가능
    public ResponseEntity<String> updateProject(@PathVariable Long id, @RequestBody ProjectUpdateDto updateDto) {
        projectService.updateProject(id, updateDto);
        return ResponseEntity.ok("수정 완료");
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getProjectDetail(@PathVariable Long id) {
        ProjectDetailRes detailRes = projectService.getProjectDetail(id);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "프로젝트 상세 조회 성공", detailRes), HttpStatus.OK);
    }

}
