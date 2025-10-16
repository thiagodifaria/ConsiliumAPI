package com.sisinnov.pms.service;

import com.sisinnov.pms.dto.request.CreateProjectRequest;
import com.sisinnov.pms.dto.response.ProjectResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectService {

    ProjectResponse create(CreateProjectRequest request);

    List<ProjectResponse> findAll();

    Optional<ProjectResponse> findById(UUID id);
}