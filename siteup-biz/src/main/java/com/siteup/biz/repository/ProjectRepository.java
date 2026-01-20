package com.siteup.biz.repository;

import com.siteup.biz.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUserId(String userId);
    List<Project> findByStatus(String status);
}
