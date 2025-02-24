package com.TTT.TTT.ListTap.projectList.repository;

import com.TTT.TTT.ListTap.projectList.domain.PrimaryFeature;
import com.TTT.TTT.ListTap.projectList.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PrimaryFeatureRepository extends JpaRepository<PrimaryFeature,Long> {
    void deleteAllByProject(Project project);
}
