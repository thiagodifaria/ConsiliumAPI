package com.sisinnov.pms.repository;

import com.sisinnov.pms.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Optional<Project> findByNameIgnoreCase(String name);

    Boolean existsByNameIgnoreCase(String name);
}