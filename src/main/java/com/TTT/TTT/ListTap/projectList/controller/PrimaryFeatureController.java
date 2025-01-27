package com.TTT.TTT.ListTap.projectList.controller;

import com.TTT.TTT.Common.CommonDto;
import com.TTT.TTT.ListTap.projectList.domain.PrimaryFeature;
import com.TTT.TTT.ListTap.projectList.dtos.PrimaryFeatureSaveReq;
import com.TTT.TTT.ListTap.projectList.service.PrimaryFeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PrimaryFeatureController {
//    의존성 주입(중요한 API가 아니라서 오토와이어드로 받음)
    @Autowired
    private PrimaryFeatureService primaryFeatureService;

//    프로젝트 리스트에 프로젝트 추가할 때 그 프로젝트에 대한 기능이 뭔지 추가할 때 호출되는 메서드
//    @GetMapping("/project/create/addfeature")
//    public ResponseEntity<?> save(PrimaryFeatureSaveReq primaryFeatureSaveReq){
//        primaryFeatureService.save(primaryFeatureSaveReq);
//        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(), "sucess","sucess"),HttpStatus.CREATED);
//    }



}
