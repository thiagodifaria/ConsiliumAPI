package com.sisinnov.pms.service.command;

import com.sisinnov.pms.dto.request.CreateTaskRequest;
import com.sisinnov.pms.dto.request.UpdateTaskRequest;
import com.sisinnov.pms.dto.response.TaskResponse;
import com.sisinnov.pms.entity.Project;
import com.sisinnov.pms.entity.Task;
import com.sisinnov.pms.enums.TaskStatus;
import com.sisinnov.pms.event.TaskCreatedEvent;
import com.sisinnov.pms.event.TaskStatusChangedEvent;
import com.sisinnov.pms.exception.BusinessException;
import com.sisinnov.pms.exception.ResourceNotFoundException;
import com.sisinnov.pms.mapper.TaskMapper;
import com.sisinnov.pms.messaging.producer.TaskEventProducer;
import com.sisinnov.pms.repository.ProjectRepository;
import com.sisinnov.pms.repository.TaskRepository;
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
public class TaskCommandService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TaskMapper taskMapper;
    private final TaskEventProducer taskEventProducer;
    private final EventStoreService eventStoreService;

    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponse create(CreateTaskRequest request) {
        log.info("COMMAND: Creating task '{}' for project: {}", request.title(), request.projectId());

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id: " + request.projectId()));

        Task task = taskMapper.toEntity(request);
        task.setProject(project);
        task = taskRepository.save(task);

        log.debug("COMMAND: Task created with ID: {}", task.getId());

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("taskId", task.getId().toString());
        eventData.put("projectId", task.getProject().getId().toString());
        eventData.put("title", task.getTitle());
        eventData.put("description", task.getDescription());
        eventData.put("status", task.getStatus().toString());
        eventData.put("priority", task.getPriority().toString());
        eventData.put("dueDate", task.getDueDate() != null ? task.getDueDate().toString() : null);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "CREATE");
        metadata.put("projectName", project.getName());

        eventStoreService.saveTaskEvent(
                EventStoreService.EventTypes.TASK_CREATED,
                task.getId(),
                eventData,
                metadata
        );

        TaskCreatedEvent event = TaskCreatedEvent.from(task);
        taskEventProducer.publishTaskCreated(event);

        return taskMapper.toResponse(task);
    }

    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponse update(UUID id, UpdateTaskRequest request) {
        log.info("COMMAND: Updating task with ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("title", task.getTitle());
        oldValues.put("description", task.getDescription());
        oldValues.put("status", task.getStatus().toString());
        oldValues.put("priority", task.getPriority().toString());
        oldValues.put("projectId", task.getProject().getId().toString());
        oldValues.put("dueDate", task.getDueDate() != null ? task.getDueDate().toString() : null);

        if (request.projectId() != null && !request.projectId().equals(task.getProject().getId())) {
            Project newProject = projectRepository.findById(request.projectId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Project not found with id: " + request.projectId()));

            task.setProject(newProject);
        }

        if (request.title() != null) {
            task.setTitle(request.title());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }
        task = taskRepository.save(task);

        log.debug("COMMAND: Task updated with ID: {}", id);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("taskId", task.getId().toString());
        eventData.put("oldValues", oldValues);
        eventData.put("newValues", Map.of(
                "title", task.getTitle(),
                "description", task.getDescription(),
                "status", task.getStatus().toString(),
                "priority", task.getPriority().toString(),
                "projectId", task.getProject().getId().toString(),
                "dueDate", task.getDueDate() != null ? task.getDueDate().toString() : null
        ));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "UPDATE");
        metadata.put("projectName", task.getProject().getName());

        eventStoreService.saveTaskEvent(
                EventStoreService.EventTypes.TASK_UPDATED,
                task.getId(),
                eventData,
                metadata
        );

        return taskMapper.toResponse(task);
    }

    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponse updateStatus(UUID id, TaskStatus newStatus) {
        log.info("COMMAND: Updating task {} status to: {}", id, newStatus);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        TaskStatus oldStatus = task.getStatus();


        task.setStatus(newStatus);
        task = taskRepository.save(task);

        log.debug("COMMAND: Task status updated from {} to {}", oldStatus, newStatus);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("taskId", task.getId().toString());
        eventData.put("oldStatus", oldStatus.toString());
        eventData.put("newStatus", newStatus.toString());
        eventData.put("title", task.getTitle());
        eventData.put("projectId", task.getProject().getId().toString());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "STATUS_CHANGE");
        metadata.put("projectName", task.getProject().getName());
        metadata.put("statusTransition", oldStatus + " -> " + newStatus);

        eventStoreService.saveTaskEvent(
                EventStoreService.EventTypes.TASK_STATUS_CHANGED,
                task.getId(),
                eventData,
                metadata
        );

        TaskStatusChangedEvent event = TaskStatusChangedEvent.from(task, oldStatus, newStatus);
        taskEventProducer.publishTaskStatusChanged(event);

        return taskMapper.toResponse(task);
    }

    @CacheEvict(value = "tasks", allEntries = true)
    public void delete(UUID id) {
        log.info("COMMAND: Soft deleting task with ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("taskId", task.getId().toString());
        eventData.put("title", task.getTitle());
        eventData.put("description", task.getDescription());
        eventData.put("status", task.getStatus().toString());
        eventData.put("priority", task.getPriority().toString());
        eventData.put("projectId", task.getProject().getId().toString());
        eventData.put("dueDate", task.getDueDate() != null ? task.getDueDate().toString() : null);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "DELETE");
        metadata.put("projectName", task.getProject().getName());
        metadata.put("deletionType", "SOFT");

        eventStoreService.saveTaskEvent(
                EventStoreService.EventTypes.TASK_DELETED,
                task.getId(),
                eventData,
                metadata
        );

        taskRepository.delete(task);
        log.debug("COMMAND: Task soft deleted with ID: {}", id);
    }

    @CacheEvict(value = "tasks", allEntries = true)
    public void hardDelete(UUID id) {
        log.warn("COMMAND: HARD DELETE task with ID: {} - IRREVERSÍVEL!", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("taskId", task.getId().toString());
        eventData.put("title", task.getTitle());
        eventData.put("description", task.getDescription());
        eventData.put("status", task.getStatus().toString());
        eventData.put("priority", task.getPriority().toString());
        eventData.put("projectId", task.getProject().getId().toString());
        eventData.put("dueDate", task.getDueDate() != null ? task.getDueDate().toString() : null);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "HARD_DELETE");
        metadata.put("projectName", task.getProject().getName());
        metadata.put("deletionType", "PERMANENT");
        metadata.put("warning", "IRREVERSIBLE_OPERATION");

        eventStoreService.saveTaskEvent(
                EventStoreService.EventTypes.TASK_DELETED,
                task.getId(),
                eventData,
                metadata
        );

        taskRepository.deleteById(id);
        log.debug("COMMAND: Task permanently deleted with ID: {}", id);
    }

}