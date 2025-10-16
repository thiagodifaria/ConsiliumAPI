package com.sisinnov.pms.service.impl;

import com.sisinnov.pms.dto.request.CreateProjectRequest;
import com.sisinnov.pms.dto.response.ProjectResponse;
import com.sisinnov.pms.entity.Project;
import com.sisinnov.pms.exception.BusinessException;
import com.sisinnov.pms.mapper.ProjectMapper;
import com.sisinnov.pms.repository.ProjectRepository;
import com.sisinnov.pms.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Override
    @Transactional
    public ProjectResponse create(CreateProjectRequest request) {
        if (projectRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Project name already exists: " + request.name());
        }

        if (request.endDate() != null && request.startDate().isAfter(request.endDate())) {
            throw new BusinessException("Start date must be before end date");
        }

        Project project = projectMapper.toEntity(request);
        project = projectRepository.save(project);

        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> findAll() {
        return projectRepository.findAll().stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProjectResponse> findById(UUID id) {
        return projectRepository.findById(id)
                .map(projectMapper::toResponse);
    }
}