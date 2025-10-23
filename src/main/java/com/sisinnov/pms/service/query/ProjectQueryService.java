package com.sisinnov.pms.service.query;

import com.sisinnov.pms.dto.response.ProjectResponse;
import com.sisinnov.pms.entity.Project;
import com.sisinnov.pms.exception.ResourceNotFoundException;
import com.sisinnov.pms.mapper.ProjectMapper;
import com.sisinnov.pms.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectQueryService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Cacheable(value = "projects", key = "#id")
    public Optional<ProjectResponse> findById(UUID id) {
        log.debug("QUERY: Finding project by ID: {}", id);

        return projectRepository.findById(id)
                .map(project -> {
                    log.debug("QUERY: Project found with ID: {}", id);
                    return projectMapper.toResponse(project);
                });
    }

    @Cacheable(value = "projects", key = "#id")
    public ProjectResponse findByIdOrThrow(UUID id) {
        log.debug("QUERY: Finding project by ID (throw if not found): {}", id);

        return projectRepository.findById(id)
                .map(projectMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    @Cacheable(
            value = "projects",
            key = "T(String).format('list:%s:%s:%s', #name, #pageable.pageNumber, #pageable.pageSize)"
    )
    public Page<ProjectResponse> findAll(Object unused, String name, Pageable pageable) {
        log.debug("QUERY: Finding all projects - name: {}, page: {}",
                name, pageable.getPageNumber());

        Page<Project> projects = projectRepository.findAll(pageable);

        log.debug("QUERY: Found {} projects", projects.getTotalElements());
        return projects.map(projectMapper::toResponse);
    }

    @Cacheable(value = "projects", key = "'count'")
    public long count(Object unused) {
        log.debug("QUERY: Counting all projects");
        return projectRepository.count();
    }

    public boolean existsById(UUID id) {
        log.debug("QUERY: Checking if project exists with ID: {}", id);
        return projectRepository.existsById(id);
    }

    @Cacheable(
            value = "projects",
            key = "T(String).format('user:%s:%s:%s', #userId, #pageable.pageNumber, #pageable.pageSize)"
    )
    public Page<ProjectResponse> findByUserId(UUID userId, Pageable pageable) {
        log.debug("QUERY: Finding projects for user: {}", userId);

        return findAll(null, null, pageable);
    }
}