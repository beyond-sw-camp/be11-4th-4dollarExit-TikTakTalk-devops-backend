package com.TTT.TTT.ListTap.projectList.repository;

import com.TTT.TTT.ListTap.projectList.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findAll(Specification<Project> specification, Pageable pageable);

    List<Project> findAll();


   //진영추가
    List<Project> findByPrimaryFeatureList_UtilityName(String utilityName);

}
