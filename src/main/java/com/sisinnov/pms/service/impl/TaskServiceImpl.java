package com.sisinnov.pms.service.impl;

import com.sisinnov.pms.dto.request.CreateTaskRequest;
import com.sisinnov.pms.dto.response.TaskResponse;
import com.sisinnov.pms.entity.Project;
import com.sisinnov.pms.entity.Task;
import com.sisinnov.pms.enums.TaskPriority;
import com.sisinnov.pms.enums.TaskStatus;
import com.sisinnov.pms.exception.BusinessException;
import com.sisinnov.pms.exception.ResourceNotFoundException;
import com.sisinnov.pms.mapper.TaskMapper;
import com.sisinnov.pms.repository.ProjectRepository;
import com.sisinnov.pms.repository.TaskRepository;
import com.sisinnov.pms.repository.specification.TaskSpecification;
import com.sisinnov.pms.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskResponse create(CreateTaskRequest request) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", request.projectId()));

        Task task = taskMapper.toEntity(request);
        task.setProject(project);
        task.setDeleted(false);

        task = taskRepository.save(task);

        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> findAll(TaskStatus status, TaskPriority priority, UUID projectId) {
        Specification<Task> spec = TaskSpecification.notDeleted();

        if (status != null) {
            spec = spec.and(TaskSpecification.withStatus(status));
        }

        if (priority != null) {
            spec = spec.and(TaskSpecification.withPriority(priority));
        }

        if (projectId != null) {
            spec = spec.and(TaskSpecification.withProjectId(projectId));
        }

        return taskRepository.findAll(spec).stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskResponse updateStatus(UUID id, TaskStatus newStatus) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        if (task.getDeleted()) {
            throw new BusinessException("Cannot update status of deleted task");
        }

        task.setStatus(newStatus);
        task = taskRepository.save(task);

        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        task.setDeleted(true);
        taskRepository.save(task);
    }
}