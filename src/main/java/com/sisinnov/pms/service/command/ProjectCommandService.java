package com.sisinnov.pms.service.command;

import com.sisinnov.pms.dto.request.CreateProjectRequest;
import com.sisinnov.pms.dto.request.UpdateProjectRequest;
import com.sisinnov.pms.dto.response.ProjectResponse;
import com.sisinnov.pms.entity.Project;
import com.sisinnov.pms.exception.ResourceNotFoundException;
import com.sisinnov.pms.mapper.ProjectMapper;
import com.sisinnov.pms.repository.ProjectRepository;
import com.sisinnov.pms.service.EventStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectCommandService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final EventStoreService eventStoreService;

    @CacheEvict(value = "projects", allEntries = true)
    public ProjectResponse create(CreateProjectRequest request) {
        log.info("COMMAND: Creating project with name: {}", request.name());

        Project project = projectMapper.toEntity(request);
        project = projectRepository.save(project);

        log.debug("COMMAND: Project created with ID: {}", project.getId());

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("projectId", project.getId().toString());
        eventData.put("name", project.getName());
        eventData.put("description", project.getDescription());
        eventData.put("startDate", project.getStartDate() != null ? project.getStartDate().toString() : null);
        eventData.put("endDate", project.getEndDate() != null ? project.getEndDate().toString() : null);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "CREATE");

        eventStoreService.saveProjectEvent(
                EventStoreService.EventTypes.PROJECT_CREATED,
                project.getId(),
                eventData,
                metadata
        );

        return projectMapper.toResponse(project);
    }

    @CacheEvict(value = "projects", allEntries = true)
    public ProjectResponse update(UUID id, UpdateProjectRequest request) {
        log.info("COMMAND: Updating project with ID: {}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("name", project.getName());
        oldValues.put("description", project.getDescription());
        oldValues.put("startDate", project.getStartDate() != null ? project.getStartDate().toString() : null);
        oldValues.put("endDate", project.getEndDate() != null ? project.getEndDate().toString() : null);

        if (request.name() != null) {
            project.setName(request.name());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        if (request.startDate() != null) {
            project.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            project.setEndDate(request.endDate());
        }
        project = projectRepository.save(project);

        log.debug("COMMAND: Project updated with ID: {}", id);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("projectId", project.getId().toString());
        eventData.put("oldValues", oldValues);
        eventData.put("newValues", Map.of(
                "name", project.getName(),
                "description", project.getDescription(),
                "startDate", project.getStartDate() != null ? project.getStartDate().toString() : null,
                "endDate", project.getEndDate() != null ? project.getEndDate().toString() : null
        ));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "UPDATE");

        eventStoreService.saveProjectEvent(
                EventStoreService.EventTypes.PROJECT_UPDATED,
                project.getId(),
                eventData,
                metadata
        );

        return projectMapper.toResponse(project);
    }

    @CacheEvict(value = "projects", allEntries = true)
    public void delete(UUID id) {
        log.info("COMMAND: Soft deleting project with ID: {}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("projectId", project.getId().toString());
        eventData.put("name", project.getName());
        eventData.put("description", project.getDescription());
        eventData.put("startDate", project.getStartDate() != null ? project.getStartDate().toString() : null);
        eventData.put("endDate", project.getEndDate() != null ? project.getEndDate().toString() : null);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "DELETE");
        metadata.put("deletionType", "SOFT");

        eventStoreService.saveProjectEvent(
                EventStoreService.EventTypes.PROJECT_DELETED,
                project.getId(),
                eventData,
                metadata
        );

        projectRepository.delete(project);
        log.debug("COMMAND: Project soft deleted with ID: {}", id);
    }

    @CacheEvict(value = "projects", allEntries = true)
    public void hardDelete(UUID id) {
        log.warn("COMMAND: HARD DELETE project with ID: {} - IRREVERSÍVEL!", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("projectId", project.getId().toString());
        eventData.put("name", project.getName());
        eventData.put("description", project.getDescription());
        eventData.put("startDate", project.getStartDate() != null ? project.getStartDate().toString() : null);
        eventData.put("endDate", project.getEndDate() != null ? project.getEndDate().toString() : null);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "HARD_DELETE");
        metadata.put("deletionType", "PERMANENT");
        metadata.put("warning", "IRREVERSIBLE_OPERATION");

        eventStoreService.saveProjectEvent(
                EventStoreService.EventTypes.PROJECT_DELETED,
                project.getId(),
                eventData,
                metadata
        );

        projectRepository.deleteById(id);
        log.debug("COMMAND: Project permanently deleted with ID: {}", id);
    }
}