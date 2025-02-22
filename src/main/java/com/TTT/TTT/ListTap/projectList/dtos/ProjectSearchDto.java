package com.TTT.TTT.ListTap.projectList.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectSearchDto {
    private Integer batch; //기수검색
    private String teamName; //팀명검색
    private String ServiceName; //서비스명 검색
}
