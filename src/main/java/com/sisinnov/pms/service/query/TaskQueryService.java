package com.sisinnov.pms.service.query;

import com.sisinnov.pms.dto.response.TaskResponse;
import com.sisinnov.pms.entity.Task;
import com.sisinnov.pms.enums.TaskPriority;
import com.sisinnov.pms.enums.TaskStatus;
import com.sisinnov.pms.exception.ResourceNotFoundException;
import com.sisinnov.pms.mapper.TaskMapper;
import com.sisinnov.pms.repository.TaskRepository;
import com.sisinnov.pms.repository.specification.TaskSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskQueryService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Cacheable(value = "tasks", key = "#id")
    public Optional<TaskResponse> findById(UUID id) {
        log.debug("QUERY: Finding task by ID: {}", id);

        return taskRepository.findById(id)
                .map(task -> {
                    log.debug("QUERY: Task found with ID: {}", id);
                    return taskMapper.toResponse(task);
                });
    }

    @Cacheable(value = "tasks", key = "#id")
    public TaskResponse findByIdOrThrow(UUID id) {
        log.debug("QUERY: Finding task by ID (throw if not found): {}", id);

        return taskRepository.findById(id)
                .map(taskMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    @Cacheable(
            value = "tasks",
            key = "T(String).format('list:%s:%s:%s', #status, #priority, #projectId)"
    )
    public List<TaskResponse> findAll(TaskStatus status, TaskPriority priority, UUID projectId) {
        log.debug("QUERY: Finding all tasks - status: {}, priority: {}, projectId: {}",
                status, priority, projectId);

        Specification<Task> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and(TaskSpecification.withStatus(status));
        }

        if (priority != null) {
            spec = spec.and(TaskSpecification.withPriority(priority));
        }

        if (projectId != null) {
            spec = spec.and(TaskSpecification.withProjectId(projectId));
        }

        List<Task> tasks = taskRepository.findAll(spec);

        log.debug("QUERY: Found {} tasks", tasks.size());
        return tasks.stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "tasks", key = "T(String).format('project:%s', #projectId)")
    public List<TaskResponse> findByProjectId(UUID projectId) {
        log.debug("QUERY: Finding tasks for project: {}", projectId);

        return findAll(null, null, projectId);
    }

    @Cacheable(value = "tasks", key = "T(String).format('status:%s', #status)")
    public List<TaskResponse> findByStatus(TaskStatus status) {
        log.debug("QUERY: Finding tasks with status: {}", status);

        return findAll(status, null, null);
    }

    @Cacheable(value = "tasks", key = "T(String).format('priority:%s', #priority)")
    public List<TaskResponse> findByPriority(TaskPriority priority) {
        log.debug("QUERY: Finding tasks with priority: {}", priority);

        return findAll(null, priority, null);
    }

    @Cacheable(value = "tasks", key = "T(String).format('count:%s:%s', #status, #projectId)")
    public long count(TaskStatus status, UUID projectId) {
        log.debug("QUERY: Counting tasks - status: {}, projectId: {}", status, projectId);

        Specification<Task> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and(TaskSpecification.withStatus(status));
        }

        if (projectId != null) {
            spec = spec.and(TaskSpecification.withProjectId(projectId));
        }

        return taskRepository.count(spec);
    }

    public boolean existsById(UUID id) {
        log.debug("QUERY: Checking if task exists with ID: {}", id);
        return taskRepository.existsById(id);
    }

    @Cacheable(value = "tasks", key = "T(String).format('due-soon:%s', #projectId)")
    public List<TaskResponse> findDueSoon(UUID projectId) {
        log.debug("QUERY: Finding tasks due soon for project: {}", projectId);

        return findAll(null, null, projectId);
    }
}